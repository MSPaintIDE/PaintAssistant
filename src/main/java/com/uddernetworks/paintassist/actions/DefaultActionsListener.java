package com.uddernetworks.paintassist.actions;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.uddernetworks.paintassist.PaintAssist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DefaultActionsListener implements ActionListener {

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultActionsListener.class);

    private PaintAssist paintAssist;
    private List<BiConsumer<List<Action>, Long>> listeners = new ArrayList<>();

    public DefaultActionsListener(PaintAssist paintAssist) {
        this.paintAssist = paintAssist;
    }

    @Override
    public void init() throws IOException {
        if (!this.paintAssist.getAuthenticator().isAuthenticated()) throw new RuntimeException("Tried to init without authentication.");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setDatabaseUrl("https://ms-paint-ide.firebaseio.com")
                .build();

        FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseApp.initializeApp(options));

        database.goOnline();

        var startingTime = System.currentTimeMillis();


        paintAssist.getAuthenticator().getTokenInfo().ifPresent(tokenInfo -> {
            var userRef = database.getReference().child("/users/" + tokenInfo.getUserId());

            userRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                    processMutation(startingTime, snapshot, userRef);
                }

                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                    processMutation(startingTime, snapshot, userRef);
                }

                @Override
                public void onChildRemoved(DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        });

    }

    @Override
    public void listen(BiConsumer<List<Action>, Long> onRun) {
        this.listeners.add(onRun);
    }

    @Override
    public void clearListeners() {
        this.listeners.clear();
    }

    private void processMutation(long startingTime, DataSnapshot snapshot, DatabaseReference userRef) {
        if (!snapshot.getKey().equals("actions")) return;

        var children = snapshot.getChildren().iterator();
        var list = new ArrayList<DataSnapshot>();
        children.forEachRemaining(list::add);

        var userRoot = snapshot.getRef().getParent();

        getValue(userRoot.child("timestamp"), value -> {
            var timestamp = value.getValue(Long.class);

            if (startingTime > timestamp) {
                LOGGER.info("Starting time was after the change time, therefore cancelling.");
                return;
            }

            var actions = list.stream()
                    .map(DataSnapshot::getValue)
                    .map(String.class::cast)
                    .map(name -> new AbstractMap.SimpleEntry<>(name, Action.fromDatabase(name)))
                    .filter(entry -> {
                        if (entry.getValue().isEmpty()) {
                            LOGGER.error("Unknown value received from database: {}. If your client up to date?", entry.getKey());
                            return false;
                        }
                        return true;
                    })
                    .map(Map.Entry::getValue)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            this.listeners.forEach(listener -> listener.accept(actions, timestamp));
        }, error -> LOGGER.error("No timestamp found!"));
    }

    private void getValue(DatabaseReference ref, Consumer<DataSnapshot> onDataChanged, Consumer<DatabaseError> onCancelled) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                onDataChanged.accept(snapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                onCancelled.accept(error);
            }
        });
    }
}
