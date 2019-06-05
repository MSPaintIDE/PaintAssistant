package com.uddernetworks.paintassist.actions;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.uddernetworks.paintassist.PaintAssist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DefaultActionsListener implements ActionListener {

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultActionsListener.class);

    private PaintAssist paintAssist;
    private Map<Action, List<BiConsumer<Action, Long>>> listeners = new HashMap<>();

    public DefaultActionsListener(PaintAssist paintAssist) {
        this.paintAssist = paintAssist;

        Arrays.stream(Action.values()).forEach(action -> this.listeners.put(action, new ArrayList<>()));
    }

    @Override
    public void init() throws IOException {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setDatabaseUrl("https://ms-paint-ide.firebaseio.com")
                .build();

        FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseApp.initializeApp(options));

        database.goOnline();

        var startingTime = System.currentTimeMillis();

        database.getReference().child("/users/" + paintAssist.getAuthenticator().getTokenInfo().getUserId() + "/").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                processMutation(startingTime, snapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                processMutation(startingTime, snapshot);
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
    }

    @Override
    public void listen(BiConsumer<Action, Long> onRun, Action... actions) {
        Stream.of(actions).map(this.listeners::get).forEach(consumers -> consumers.add(onRun));
    }

    private void processMutation(long startingTime, DataSnapshot snapshot) {
        var children = snapshot.getChildren().iterator();
        if (!children.hasNext()) return;
        long actionAt = children.next().getValue(Long.class);

        if (startingTime > actionAt) return;

        var databaseName = snapshot.getKey();
        Action.fromDatabase(databaseName).ifPresentOrElse(
                action -> this.listeners.get(action).forEach(consumer -> consumer.accept(action, actionAt)),
                () -> LOGGER.error("Unknown value received from database: {}. If your client up to date?", databaseName));
    }
}
