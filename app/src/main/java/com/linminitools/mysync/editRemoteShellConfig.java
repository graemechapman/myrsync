package com.linminitools.mysync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import static com.linminitools.mysync.MainActivity.appContext;
import static com.linminitools.mysync.MainActivity.configs;
import static com.linminitools.mysync.MainActivity.getPath;
import static java.nio.CharBuffer.wrap;

public class editRemoteShellConfig extends addRemoteShellConfig {

    public addRemoteShellConfig r;

@Override
    protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);


    ViewGroup vg = findViewById(android.R.id.content);
    Context ctx = this.getBaseContext();
    r = new addRemoteShellConfig(ctx,vg);
    setContentView(r.vi);

    Intent i = getIntent();
    final int p = i.getIntExtra("pos", -1);
    RS_Configuration config = configs.get(p);

    r.user.setText(config.rs_user);
    r.remote_host_ip.setText(config.rs_ip);
    r.destination.setText(config.rs_dest);
    r.tv_path.setVisibility(View.VISIBLE);
    r.tv_path.setText("Selected Path: "+ config.local_path);
    Button execute = r.vi.findViewById(R.id.bt_execute);

    ImageButton rsync_help = r.rsync_help;
    ImageButton ssh_help = r.ssh_keys_help;
    rsync_help.setImageResource(R.drawable.info);
    ssh_help.setImageResource(R.drawable.help);

    r.options=(config.rs_options) ;

    if (r.options == "-") {
        r.options = "";
    }

    if (!r.options.isEmpty()) {
        String rs_options = r.options.substring(1);
        for (char x : rs_options.toCharArray()) {
            int resID = getResources().getIdentifier("cb_" + String.valueOf(x), "id", getPackageName());
            CheckBox cb = findViewById(resID);
            cb.setChecked(true);
        }
    }

    r.save_rsh.setEnabled(true);
    r.view_cmd_bt.setEnabled(true);
    execute.setEnabled(true);

    final int id=config.id;

    r.save_rsh.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            action2Config(v,id,p,1,r);
        }
    });

    execute.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            action2Config(v, id, p,2,r);
        }
    });

    r.add_path.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            action2Config(v,id,p,3,r);
        }
    });

    r.view_cmd_bt.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            action2Config(v,id,p,4,r);
        }
    });

    r.generate_ssh.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String k;
            k=generate_keys(v);
            String public_key_path=appContext.getApplicationInfo().dataDir + "/rsa_key.pub";
            File rsa_pub = new File(appContext.getApplicationInfo().dataDir + "/rsa_key.pub");
            try {
                FileWriter fw = new FileWriter(rsa_pub);
                fw.write(k);
                fw.close();
                AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                        .setTitle("KeyPair Generation")
                        .setMessage("The location of the public key is \n "+public_key_path+" \nThis file must be imported in your server")
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
            catch( IOException e){
                Log.d("IOEexcepton",e.toString());
            }
        }
    });




}


    protected void action2Config(View v, int id, int position, int request,addRemoteShellConfig r){
        Map<String,String> configMap=r.processForm(v);

        if (!configMap.get("rs_ip").isEmpty() && !configMap.get("rs_user").isEmpty() &&
                !configMap.get("rs_dest").isEmpty() && !configMap.get("local_path").isEmpty()) {

            for (RS_Configuration c : configs){

                if (c.id==id){
                    c.rs_ip = configMap.get("rs_ip");
                    c.rs_user = configMap.get("rs_user");
                    c.rs_options = configMap.get("rs_options");
                    c.local_path = configMap.get("local_path");
                    c.rs_dest = configMap.get("rs_dest");
                    c.mode = Integer.parseInt(configMap.get("rs_mode"));


                    if (request==1) {
                        c.saveToDisk();
                        Log.d("Configuration ID",c.rs_user + " " + c.rs_options);
                        CharSequence text = "Configuration saved";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(appContext, text, duration);
                        toast.show();
                        this.finish();
                    }
                    else if (request==2){
                        c.executeConfig(getApplication().getBaseContext());

                        CharSequence text = "Rsync Job Started";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(appContext, text, duration);
                        toast.show();
                        this.finish();
                    }

                    else if (request==3){
                        Uri selectedUri = Uri.parse(Environment.getDataDirectory().toString());
                        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        Intent i2= Intent.createChooser(i,"Choose Directory");
                        startActivityForResult(i2,1);
                    }

                    else if (request==4){
                        r.viewCommand(v);
                    }

                    break;

                }
            }

        }
        else{


            CharSequence text = "Configuration is not Complete!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(appContext, text, duration);
            toast.show();
            this.finish();
        }


    }

    public String generate_keys(View v) {
        try {

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            String KEY_ALIAS = "SSH_CREDENTIALS";

            // Generate the RSA key pairs
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                // Generate a key pair for encryption
                Log.d("SSH", "ALIAS EXISTS");
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 30);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(KEY_ALIAS)
                        .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                        .setSerialNumber(BigInteger.TEN)
                        .setStartDate(start.getTime())
                        .setKeyType(KeyProperties.KEY_ALGORITHM_RSA)
                        .setEndDate(end.getTime())
                        .build();
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                kpg.initialize(spec);
                KeyPair kp = kpg.generateKeyPair();
                byte publickey[] = kp.getPublic().getEncoded();

                KeyFactory fact = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec sp = fact.getKeySpec(kp.getPublic(), X509EncodedKeySpec.class);
                String publicKeyString = Base64.encodeToString(sp.getEncoded(), Base64.DEFAULT);
                String pub = Base64.encodeToString(publickey, Base64.DEFAULT);
                String pem = "-----BEGIN PUBLIC KEY-----\n" + wrap(pub) + "-----END PUBLIC KEY-----\n";
                return pem;
            } else {
                PublicKey publicKey = keyStore.getCertificate(KEY_ALIAS).getPublicKey();
                byte publickey[] = keyStore.getCertificate(KEY_ALIAS).getPublicKey().getEncoded();

                String pub = Base64.encodeToString(publickey, Base64.DEFAULT);
                String pem = "-----BEGIN PUBLIC KEY-----\n" + wrap(pub) + "-----END PUBLIC KEY-----\n";
                return pem;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected path

                Uri pathUri = data.getData();
                Uri dirUri = DocumentsContract.buildDocumentUriUsingTree(pathUri, DocumentsContract.getTreeDocumentId(pathUri));
                String local_path = getPath(this, dirUri);


                SharedPreferences path_prefs = getSharedPreferences("Rsync_Config_path", MODE_PRIVATE);
                SharedPreferences.Editor path_prefseditor = path_prefs.edit();
                path_prefseditor.putString("local_path", local_path);
                path_prefseditor.apply();

                Log.d("ONACTIVITYRESULT",local_path);


                r.Update_view(local_path);


            }

        }
    }



}
