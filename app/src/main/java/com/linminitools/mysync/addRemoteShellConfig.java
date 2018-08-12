package com.linminitools.mysync;

import android.content.Context;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import static com.linminitools.mysync.MainActivity.PackageName;
import static com.linminitools.mysync.MainActivity.appContext;
import static com.linminitools.mysync.MainActivity.configs;
import static java.nio.CharBuffer.wrap;

public class addRemoteShellConfig extends AppCompatActivity {

    EditText remote_host_ip, user,destination;
    TextView tv_path,view_cmd;
    String options;
    Button save_rsh,generate_ssh,add_path,view_cmd_bt;
    ImageButton rsync_help, ssh_keys_help;
    LayoutInflater inflater;
    View vi= null;
    Context context;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public addRemoteShellConfig(){
    }

    public addRemoteShellConfig(Context ctx,ViewGroup parent){
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vi = inflater.inflate(R.layout.add_remote_shell_config, parent, false);
        context = ctx;

        remote_host_ip = vi.findViewById(R.id.et_rsh_host);
        user= vi.findViewById(R.id.et_rsh_user);
        save_rsh= vi.findViewById(R.id.bt_save);
        destination=vi.findViewById(R.id.et_remote_dir);
        tv_path=vi.findViewById(R.id.tv_selected_path);
        add_path=vi.findViewById(R.id.bt_add_local_path);
        view_cmd=vi.findViewById(R.id.tv_rs_cmd_View);
        view_cmd_bt=vi.findViewById(R.id.bt_view);
        rsync_help = vi.findViewById(R.id.bt_help_rsync);
        ssh_keys_help=vi.findViewById(R.id.bt_keys_help);

        save_rsh.setEnabled(false);
        view_cmd_bt.setEnabled(false);

        generate_ssh = vi.findViewById(R.id.bt_generate_ssh_keys);
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
                String pub= Base64.encodeToString(publickey,Base64.DEFAULT);
                String pem = "-----BEGIN PUBLIC KEY-----\n" + wrap(pub) + "-----END PUBLIC KEY-----\n";
                return pem;
            }

            else{
                PublicKey publicKey= keyStore.getCertificate(KEY_ALIAS).getPublicKey();
                byte publickey[] = keyStore.getCertificate(KEY_ALIAS).getPublicKey().getEncoded();

                String pub= Base64.encodeToString(publickey,Base64.DEFAULT);
                String pem = "-----BEGIN PUBLIC KEY-----\n" + wrap(pub) + "-----END PUBLIC KEY-----\n";
                return pem;
            }








            /*

            AlertDialog.Builder alertDialogBuilder =
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Your KEYS")
                            .setMessage("I want to show the keys here")
                            .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                    ;
            alertDialogBuilder.show();



            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyStore.getDefaultType());
            keyPairGenerator.initialize(2048);
            KeyPair kp= keyPairGenerator.generateKeyPair();

            PrivateKey priv= kp.getPrivate();
            PublicKey pub= kp.getPublic();

            KeyStore ks= KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);

            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(priv.getEncoded());

            KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry("privateKey", pivKey);

            ks.setEntry("PrivateKey",(KeyStore.PrivateKeyEntry)priv,null);
                */
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Map<String,String> processForm(View v){
        if (vi==null) {
            if (inflater==null) inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.add_remote_shell_config, (ViewGroup) findViewById(android.R.id.content), false);
        }

        Map<String,String> configMap = new HashMap<String,String>();

        options="-";

        String[] availableOptions={"a","r","z","v","n","p","t","O","q","m","u","g"};
        Log.d("PACKAGENAME",PackageName);
        for(String check_box : availableOptions){

            int resID = appContext.getResources().getIdentifier("cb_" + check_box, "id", PackageName);
            Log.d("RESID",String.valueOf(resID));
            CheckBox cb = vi.findViewById(resID);
            if(cb.isChecked()){
                options=options.concat(check_box);
            }
        }

        if (options=="-"){options="";}

        String rs_user = String.valueOf(this.user.getText());
        String rs_ip = String.valueOf(this.remote_host_ip.getText());
        String local_path = this.context.getSharedPreferences("Rsync_Config_path", MODE_PRIVATE).getString("local_path", "");
        String destination = String.valueOf(this.destination.getText());

        configMap.put("rs_ip", rs_ip);
        configMap.put("rs_user", rs_user);
        configMap.put("rs_mode", "1");
        configMap.put("rs_dest",destination);
        configMap.put("local_path", local_path);
        configMap.put("rs_options", options);


        return configMap;
    }

    protected void Update_view(String local_path){

        if (!local_path.isEmpty()) {
            tv_path.setText("Selected Path: "+ local_path);
            tv_path.setVisibility(View.VISIBLE);

            add_path.setText("Change Path");

            save_rsh.setEnabled(true);
            view_cmd.setEnabled(true);

        }
    }

    protected boolean saveConfig(View v){
        Map<String,String> configMap=this.processForm(v);

        String rs_options = configMap.get("rs_options");
        String rs_user = configMap.get("rs_user");
        String rs_ip = configMap.get("rs_ip");
        String rs_dest = configMap.get("rs_dest");
        String local_path = configMap.get("local_path");


        if (!rs_ip.isEmpty() && !rs_user.isEmpty() && !local_path.isEmpty() && !rs_dest.isEmpty()) {


            int counter=configs.size()+1;

            RS_Configuration config = new RS_Configuration(counter);
            config.mode=(Integer.parseInt(configMap.get("rs_mode")));
            config.rs_ip = rs_ip;
            config.rs_user = rs_user;
            config.rs_options = rs_options;
            config.local_path = local_path;
            config.saveToDisk();
            configs.add(config);
            return true;

        }
        else{


            CharSequence text = "Configuration is not Complete!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return false;
        }


    }

    public void viewCommand(View v){

        Map<String,String> configMap=this.processForm(v);

        String options = configMap.get("rs_options");
        String rs_user = configMap.get("rs_user");
        String rs_ip = configMap.get("rs_ip");
        String rs_dest = configMap.get("rs_dest");
        String local_path = configMap.get("local_path");


        TextView tv= this.vi.findViewById(R.id.tv_rs_cmd_View);

        String Rsync_command = "rsync "+options+" "+ local_path+" "+rs_user+"@"+rs_ip+":"+rs_dest;

        tv.setText(Rsync_command);

    }



}
