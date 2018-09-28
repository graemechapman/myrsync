package com.linminitools.myrsync;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;
import static com.linminitools.myrsync.MainActivity.appContext;


class RS_Configuration implements Comparable<RS_Configuration>{

    String rs_ip;
    String rs_user;
    String rs_module;
    String rs_options;
    String local_path;
    String name;
    Long addedOn;
    String rs_port="873";
    final int id;

    RS_Configuration(int id){
        this.id=id;
        this.name="Config "+String.valueOf(id);
    }
    
    void saveToDisk(){
        
        SharedPreferences prefs = appContext.getSharedPreferences("configs", MODE_PRIVATE);
        SharedPreferences.Editor prefseditor = prefs.edit();
        this.addedOn= Calendar.getInstance().getTimeInMillis();

        prefseditor.putInt("rs_id_"+String.valueOf(this.id),this.id);
        prefseditor.putString("rs_name_"+String.valueOf(this.id),name);
        prefseditor.putString("rs_options_"+String.valueOf(this.id),rs_options);
        prefseditor.putString("rs_user_"+String.valueOf(this.id),rs_user);
        prefseditor.putString("rs_module_"+String.valueOf(this.id),rs_module);
        prefseditor.putString("rs_ip_"+String.valueOf(this.id),rs_ip);
        prefseditor.putString("rs_port_"+String.valueOf(this.id),rs_port);
        prefseditor.putString("local_path_"+String.valueOf(this.id),local_path);
        prefseditor.putString("last_result_"+String.valueOf(this.id),"Never Run");
        prefseditor.putString("last_run_"+String.valueOf(this.id),"Never Run");
        prefseditor.putLong("rs_addedon_"+String.valueOf(this.id),addedOn);

        prefseditor.apply();
        
    }
    
    void deleteFromDisk(){
        SharedPreferences prefs = appContext.getSharedPreferences("configs", MODE_PRIVATE);
        SharedPreferences.Editor prefseditor = prefs.edit();

        prefseditor.remove("rs_name_"+String.valueOf(this.id));
        prefseditor.remove("rs_id_"+String.valueOf(this.id));
        prefseditor.remove("rs_options_"+String.valueOf(this.id));
        prefseditor.remove("rs_user_"+String.valueOf(this.id));
        prefseditor.remove("rs_module_"+String.valueOf(this.id));
        prefseditor.remove("rs_ip_"+String.valueOf(this.id));
        prefseditor.remove("rs_port_"+String.valueOf(this.id));
        prefseditor.remove("local_path_"+String.valueOf(this.id));
        prefseditor.remove("last_result_"+String.valueOf(this.id));
        prefseditor.remove("last_run_"+String.valueOf(this.id));
        prefseditor.remove("rs_addedon_"+String.valueOf(this.id));
        prefseditor.apply();
    }

    public int compareTo(@NonNull RS_Configuration c){
        return Long.compare(this.addedOn,c.addedOn);
    }

    void executeConfig(final Context context){

        send_notification(context);
        final SharedPreferences prefs =  context.getSharedPreferences("Rsync_Command_build", MODE_PRIVATE);

        final int id = this.id;

        final String options = this.rs_options;
        final String log = prefs.getString("log",context.getApplicationInfo().dataDir+"/logfile.log");
        final String local_path=this.local_path;

        final String cmd = "rsync://"+this.rs_user +"@"+this.rs_ip+":"+this.rs_port+"/"+this.rs_module;



            Thread t = new Thread(){
                @SuppressLint("ApplySharedPref")
                @Override
                public void run() {
                    try {
                        SharedPreferences pref = context.getSharedPreferences("CMD",MODE_PRIVATE);
                        SharedPreferences.Editor pref_Edit= pref.edit();

                        pref_Edit.putBoolean("is_running",true);
                        pref_Edit.commit();
                        String rsync_bin= context.getSharedPreferences("Install",MODE_PRIVATE).getString("rsync_binary",".");

                        ProcessBuilder p = new ProcessBuilder(rsync_bin,options,"--log-file",log,local_path,cmd);
                        //p.redirectErrorStream(true);

                        Map<String, String> env = p.environment();
                        env.put("PATH", "/su/bin:/sbin:/vendor/bin:/system/sbin:/system/bin:/su/xbin:/system/xbin");
                        p.directory(new File(context.getApplicationInfo().dataDir));
                        Process process=p.start();

                        //BufferedReader std_output = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        BufferedReader std_error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                        //StringBuilder builder_1 = new StringBuilder();
                        StringBuilder builder_2 = new StringBuilder();
                        //String out_line = null;
                        String err_line;

                        while ( (err_line = std_error.readLine()) != null) {
                            builder_2.append(err_line);
                            builder_2.append(System.getProperty("line.separator"));
                        }
                        /*
                        while ( (out_line = std_output.readLine()) != null) {
                            builder_1.append(out_line);
                            builder_1.append(System.getProperty("line.separator"));
                        }
                        */
                        String result = builder_2.toString();

                        if (result.equals("")) result="OK";
                        else result="Warning! Check Log!";

                        SharedPreferences prefs =  context.getSharedPreferences("configs", MODE_PRIVATE);
                        SharedPreferences.Editor prefseditor = prefs.edit();
                        prefseditor.putString("last_result_"+String.valueOf(id),result);


                        Calendar cal = Calendar.getInstance();
                        long time = cal.getTimeInMillis();
                        Locale current_locale = context.getResources().getConfiguration().locale;
                        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd/MM HH:mm",current_locale);
                        prefseditor.putString("last_run_"+String.valueOf(id),formatter.format(time));
                        prefseditor.commit();
                        pref_Edit.putBoolean("is_running",false);
                        pref_Edit.commit();


                    }


                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            t.start();



    }

    private void send_notification(Context ctx) {

        SharedPreferences set_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Locale current_locale = ctx.getResources().getConfiguration().locale;
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd/MM HH:mm", current_locale);

        String message = "Rsync configuration " + this.name + " started on " + formatter.format(Calendar.getInstance().getTime());

        Boolean Notifications_enabled = set_prefs.getBoolean("notifications", true);
        Boolean Vibration_enabled = set_prefs.getBoolean("vibrate", false);
        Uri ring_path = Uri.parse(set_prefs.getString("ringtone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).getPath()));

        Log.d("RING_PATH", ring_path.toString());
        Log.d("Notifications_enabled", String.valueOf(Notifications_enabled));
        Log.d("VIBRATE", String.valueOf(Vibration_enabled));

        if (Notifications_enabled) {
            String new_id="";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) new_id= createNotificationChannel(ctx);

            Intent intent = new Intent(ctx, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder not = new NotificationCompat.Builder(ctx, new_id);

            not.setContentTitle("myRSync Job Started")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setSound(ring_path)
                    .setCategory(NotificationCompat.CATEGORY_EVENT)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            if (Vibration_enabled) not.setVibrate(new long[]{300, 600});

            Objects.requireNonNull(nm).notify(0, not.build());

        }
    }

        @TargetApi(Build.VERSION_CODES.O)
        private String createNotificationChannel(Context ctx){

            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            SharedPreferences set_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

            Boolean Vibration_enabled = set_prefs.getBoolean("vibrate",false);
            Uri ring_path=Uri.parse(set_prefs.getString("ringtone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).getPath()));


            if (nm != null) {
                for (NotificationChannel nc : nm.getNotificationChannels())
                    nm.deleteNotificationChannel(nc.getId());


                String new_id = String.valueOf(new Random().nextLong());


                Log.d("CHANNELS", nm.getNotificationChannels().toString());
                Log.d("VIBRATION ENABLED",String.valueOf(Vibration_enabled));

                AudioAttributes aa = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION).build();

                NotificationChannel channel = new NotificationChannel(new_id, "Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Notify Job Start");
                channel.setSound(ring_path, aa);
                channel.enableLights(true);
                channel.setLightColor(Color.WHITE);

                if (Vibration_enabled) {
                    channel.enableVibration(true);
                    channel.setVibrationPattern(new long[]{300, 600});
                } else {
                    channel.enableVibration(false);
                }

                nm.createNotificationChannel(channel);
                Log.d("CHANNEL_DETAILS", nm.getNotificationChannels().toString());

                return new_id;
            }
            return "";
        }

    }

