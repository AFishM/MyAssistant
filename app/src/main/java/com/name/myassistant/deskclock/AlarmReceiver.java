package com.name.myassistant.deskclock;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.util.Log;

import com.name.myassistant.R;
import com.name.myassistant.deskclock.v.AlarmAlertActivity;
import com.name.myassistant.deskclock.v.AlarmAlertFullScreenActivity;
import com.name.myassistant.deskclock.v.SetAlarmActivity;
import com.name.myassistant.util.LogUtil;

/**
 * Glue class: connects AlarmAlertActivity IntentReceiver to AlarmAlertActivity
 * activity.  Passes through Alarm ID.
 */
public class AlarmReceiver extends BroadcastReceiver {

    /** If the alarm is older than STALE_WINDOW, ignore.  It
        is probably the result of a time or timezone change */
    private final static int STALE_WINDOW = 30 * 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Alarms.ALARM_KILLED.equals(intent.getAction())) {
            // The alarm has been killed, update the notification
            updateNotification(context, (Alarm)
                    intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA),
                    intent.getIntExtra(Alarms.ALARM_KILLED_TIMEOUT, -1));
            return;
        } else if (Alarms.CANCEL_SNOOZE.equals(intent.getAction())) {
            Alarms.saveSnoozeAlert(context, -1, -1);
            return;
        } else if (!Alarms.ALARM_ALERT_ACTION.equals(intent.getAction())) {
            // Unknown intent, bail
            LogUtil.d("xzx","Unknown intent");
            return;
        }

        Alarm alarm = null;
        // Grab the alarm from the intent. Since the remote AlarmManagerService
        // fills in the Intent to add some extra data, it must unparcel the
        // Alarm object. It throws a ClassNotFoundException when unparcelling.
        // To avoid this, do the marshalling ourselves.
        final byte[] data = intent.getByteArrayExtra(Alarms.ALARM_RAW_DATA);
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            alarm = Alarm.CREATOR.createFromParcel(in);
        }

        if (alarm == null) {
            Alarms.setNextAlert(context);
            return;
        }

        // Disable the snooze alert if this alarm is the snooze.
        Alarms.disableSnoozeAlert(context, alarm.id);
        // Disable this alarm if it does not repeat.
        if (!alarm.daysOfWeek.isRepeatSet()) {
            Alarms.enableAlarm(context, alarm.id, false);
        } else {
            // Enable the next alert if there is one. The above call to
            // enableAlarm will call setNextAlert so avoid calling it twice.
            Alarms.setNextAlert(context);
        }

        // Intentionally verbose: always log the alarm time to provide useful
        // information in bug reports.
        long now = System.currentTimeMillis();

        // Always verbose to track down time change problems.
        if (now > alarm.time + STALE_WINDOW) {
            LogUtil.v("xzx", "Ignoring stale alarm");
            return;
        }

        // Maintain a cpu wake lock until the AlarmAlertActivity and AlarmKlaxonService can
        // pick it up.
        AlarmAlertWakeLock.acquireCpuWakeLock(context);

        /* Close dialogs and window shade */
        // FIXME: 16/4/24 这个不知道干嘛的
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);


        // Play the alarm alert and vibrate the device.
        Intent playAlarmIntent = new Intent(Alarms.ALARM_ALERT_ACTION);
        playAlarmIntent.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        context.startService(playAlarmIntent);

        // Trigger a notification that, when clicked, will show the alarm alert
        // dialog. No need to check for fullscreen since this will always be
        // launched from a user action.
        Intent notifyIntent = new Intent(context, AlarmAlertActivity.class);
        notifyIntent.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, alarm.id, notifyIntent, 0);

        // Use the alarm's label or the default label as the ticker text and
        // main text of the notification.
        String label = alarm.getLabelOrDefault(context);

        Notification notification=new Notification.Builder(context)
                .setTicker(label)
                .setContentTitle(label)
                .setContentText(context.getString(R.string.alarm_notify_text))
                .setSmallIcon(R.drawable.stat_notify_alarm)
                .setWhen(alarm.time)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build();

        notification.flags |= Notification.FLAG_SHOW_LIGHTS
                | Notification.FLAG_ONGOING_EVENT;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        // Decide which activity to start based on the state of the keyguard.
        Class activityClass = AlarmAlertActivity.class;
        KeyguardManager km = (KeyguardManager) context.getSystemService(
                Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            // Use the full screen activity for security.
            activityClass = AlarmAlertFullScreenActivity.class;
        }
        // NEW: Embed the full-screen UI here. The notification manager will
        // take care of displaying it if it's OK to do so.
        Intent alarmAlert = new Intent(context, activityClass);
        alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        notification.fullScreenIntent = PendingIntent.getActivity(context, alarm.id, alarmAlert, 0);

        // Send the notification using the alarm id to easily identify the
        // correct notification.
        NotificationManager notificationManager = getNotificationManager(context);
        notificationManager.notify(alarm.id, notification);
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void updateNotification(Context context, Alarm alarm, int timeout) {
        NotificationManager nm = getNotificationManager(context);

        // If the alarm is null, just cancel the notification.
        if (alarm == null) {
            if (true) {
                Log.v("wangxianming", "Cannot update notification for killer callback");
            }
            return;
        }

        // Launch SetAlarmActivity when clicked.
        Intent viewAlarm = new Intent(context, SetAlarmActivity.class);
        viewAlarm.putExtra(Alarms.ALARM_ID, alarm.id);
        PendingIntent intent =
                PendingIntent.getActivity(context, alarm.id, viewAlarm, 0);

        // Update the notification to indicate that the alert has been
        // silenced.
        String label = alarm.getLabelOrDefault(context);
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, alarm.time);
//        n.setLatestEventInfo(context, label,
//                context.getString(R.string.alarm_alert_alert_silenced, timeout),
//                intent);
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        // We have to cancel the original notification since it is in the
        // ongoing section and we want the "killed" notification to be a plain
        // notification.
        nm.cancel(alarm.id);
        nm.notify(alarm.id, n);
    }
}
