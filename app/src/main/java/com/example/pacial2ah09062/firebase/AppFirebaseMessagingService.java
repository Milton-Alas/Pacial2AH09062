package com.example.pacial2ah09062.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.pacial2ah09062.R;
import com.example.pacial2ah09062.activities.HomeActivity;
import com.example.pacial2ah09062.utils.PreferenceManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "AppFCMService";
    private static final String CHANNEL_ID = "orders_channel";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token FCM: " + token);

        // Si el usuario está logueado, actualizar el token en Firestore
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        String currentUserEmail = preferenceManager.getCurrentUserEmail();
        if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
            FirebaseManager firebaseManager = new FirebaseManager();
            firebaseManager.updateUserFcmToken(currentUserEmail, token, null);
        } else {
            Log.d(TAG, "No hay usuario logueado; el token se actualizará cuando inicie sesión");
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = null;
        String body = null;
        String orderId = null;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        if (remoteMessage.getData() != null && !remoteMessage.getData().isEmpty()) {
            if (title == null) {
                title = remoteMessage.getData().get("title");
            }
            if (body == null) {
                body = remoteMessage.getData().get("body");
            }
            orderId = remoteMessage.getData().get("orderId");
        }

        if (title == null) {
            title = getString(R.string.app_name);
        }
        if (body == null) {
            body = "Tienes una actualización de tu pedido";
        }

        showOrderNotification(title, body, orderId);
    }

    private void showOrderNotification(String title, String message, String orderId) {
        createNotificationChannel();

        // Por ahora abrimos HomeActivity; en el futuro se puede abrir OrderDetailActivity con el orderId
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (orderId != null) {
            intent.putExtra("orderId", orderId);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager == null) return;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Actualizaciones de pedidos",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones sobre el estado de tus pedidos");
            notificationManager.createNotificationChannel(channel);
        }
    }
}
