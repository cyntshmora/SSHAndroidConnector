package com.tgd.javasshtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    TextView shellOutput;
    Session session;
    EditText commandEditTxt;
    String host, username, password;
    Integer port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shellOutput = findViewById(R.id.textView);
        commandEditTxt = findViewById(R.id.editTxtCommand);
        Button btn = findViewById(R.id.button);
        shellOutput.setMovementMethod(new ScrollingMovementMethod());

        // gets connection data from intent
        Intent intent = getIntent();
        host = intent.getStringExtra("host");
        port = Integer.parseInt(intent.getStringExtra("port"));
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("password");

        //start execution of ssh commands
        Thread thread = new Thread(() -> {
            try {
                JSch jsch = new JSch();
                session = jsch.getSession(username, host, port);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setTimeout(100000);
                session.connect();
                executeSSHCommand("ipconfig");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        btn.setOnClickListener(v -> new Thread(()->{
            //cierro el teclado
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(commandEditTxt.getApplicationWindowToken(), 0);
            try {
                executeSSHCommand(commandEditTxt.getText().toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start());
    }

    public void executeSSHCommand(String command){
        try{
            ChannelExec channel = (ChannelExec)session.openChannel("exec");
            StringBuilder outputBuffer = new StringBuilder();
            StringBuilder errorBuffer = new StringBuilder();
            InputStream in = channel.getInputStream();
            InputStream err = channel.getExtInputStream();
            channel.setCommand(command);
            channel.connect();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    outputBuffer.append(new String(tmp, 0, i));
                }
                while (err.available() > 0) {
                    int i = err.read(tmp, 0, 1024);
                    if (i < 0) break;
                    errorBuffer.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if ((in.available() > 0) || (err.available() > 0)) continue;
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }
            setText(shellOutput,"\n\nCommand: ipconfig\n");
            System.out.println("output: " + outputBuffer.toString());
            System.out.println("error: " + errorBuffer.toString());
            setText(shellOutput, "\noutput: " + outputBuffer.toString());
            setText(shellOutput,"\nerror: " + errorBuffer.toString());
            channel.disconnect();
            // show success in UI with a snackbar alternatively use a toast
        }
        catch(JSchException | IOException e){
            // show the error in the UI
            e.printStackTrace();
        }
    }
    private void setText(final TextView text,final String value){
        runOnUiThread(() -> {
            text.append(value);
            final Layout layout = text.getLayout();
            if(layout != null){
                int scrollDelta = layout.getLineBottom(text.getLineCount() - 1)
                        - text.getScrollY() - text.getHeight();
                if(scrollDelta > 0)
                    text.scrollBy(0, scrollDelta);
            }
        });
    }
}