package pt.ulisboa.tecnico.cmov.p2photo.wifidirect;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ListAlbumsActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ListPhotosActivity;
import pt.ulisboa.tecnico.cmov.p2photo.data.GlobalVariables;
import pt.ulisboa.tecnico.cmov.p2photo.data.ListAlbumsAdapter;
import pt.ulisboa.tecnico.cmov.p2photo.data.Member;
import pt.ulisboa.tecnico.cmov.p2photo.data.Photo;
import pt.ulisboa.tecnico.cmov.p2photo.data.PhotoToSend;
import pt.ulisboa.tecnico.cmov.p2photo.data.Utils;
import pt.ulisboa.tecnico.cmov.p2photo.serverapi.ServerAPI;

public class WifiDirectManager {

    private final ListAlbumsAdapter adapter;
    private GlobalVariables globalVariables;
    private Context context;
    public static final String TAG = "WifiDirectManager";

    private static SimWifiP2pManager mManager = null;
    private static SimWifiP2pManager.Channel mChannel = null;
    private static Messenger mService = null;
    private static boolean mBound = false;
    private static SimWifiP2pSocketServer mSrvSocket = null;
    private static SimWifiP2pBroadcastReceiver mReceiver;

    private static ArrayList<PhotoToSend> photos = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("serviceConnected","connected");
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(context, context.getMainLooper(), null);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("serviceDisconnected","disconnected");
            mService = null;
            mManager = null;
            mChannel = null;
            mBound = false;
        }
    };
    private String fileID = "";

    public WifiDirectManager(Context listAlbumsActivity, ListAlbumsAdapter adapter) {
        this.context= listAlbumsActivity;
        this.adapter = adapter;

        if(mBound) {
            return;
        }


        SimWifiP2pSocketManager.Init(context);
        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);

        mReceiver = new SimWifiP2pBroadcastReceiver((ListAlbumsActivity) context);
        this.context.registerReceiver(mReceiver, filter);

        this.globalVariables = (GlobalVariables) context.getApplicationContext();

    }

    public void initiateWifi() {
        if(mBound)
            return;

        bindService();

        new IncommingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void requestGroupInfo() {
        if (mBound)
            mManager.requestGroupInfo(mChannel, (SimWifiP2pManager.GroupInfoListener) context);
    }

    public void send(String virtIp, String albumName, ListPhotosActivity listPhotosActivity) {
        new SendCommTask(virtIp,albumName,listPhotosActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public ArrayList<Member> onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo, ArrayList<Member> membersInGroup) {
        // compile list of network members
        StringBuilder peersStr = new StringBuilder();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null)?"??":device.getVirtIp()) + ")\n";
            peersStr.append(devstr);
            //connectToPeer(device.getVirtIp());

            if(!membersInGroup.contains(new Member(deviceName))) {
                membersInGroup.add(new Member(deviceName, device.getVirtIp(),"qlqrcoisa"));
                //send(device.getVirtIp(),"givemealbums");
            }
        }

        // display list of network members
        new AlertDialog.Builder(context)
                .setTitle("Devices in WiFi Network")
                .setMessage(peersStr.toString())
                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();



        return membersInGroup;
    }

    public void bindService() {
        Intent intent = new Intent(context, SimWifiP2pService.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
    }

    public void unbindService(){
        unregisterReceiver();
        context.unbindService(mConnection);
        try {
            mSrvSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBound = false;

    }



    public class IncommingCommTask extends AsyncTask<Void, String, Void> {


        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");

            try {
                mSrvSocket = new SimWifiP2pSocketServer(
                        Integer.parseInt(context.getString(R.string.port)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    try {
                        ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                        String album = (String) in.readObject();
                        ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());

                        String fileID = adapter.getFileID(album);

                        List<Photo> photos = new ArrayList<>();
                        if( (fileID != null) && !(fileID.equals("null")))
                         photos = globalVariables.getFileManager().getAlbumPhotos(fileID);

                        ArrayList<PhotoToSend> photosToSend = new ArrayList<>();

                        for(Photo photo : photos){
                              if (photo.isMine()){
                                  photosToSend.add(new PhotoToSend(photo.getUrl(), Utils.encodeBitmap(photo.getBitmap())));
                              }
                        }

                        Log.d(TAG, "photosToSend: " + photosToSend.toString());
                        out.writeObject(photosToSend);


                    } catch (IOException e) {
                        Log.d("Error reading socket:", e.getMessage());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        sock.close();
                    }
                } catch (IOException e) {
                    Log.d("Error socket:", e.getMessage());
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "IncommingCommTask: interrupted");
            return null;
        }

    }

    public class SendCommTask extends AsyncTask<String, String, ArrayList<PhotoToSend>> {

        private final ListPhotosActivity context;
        private SimWifiP2pSocket mCliSocket = null;
        String peer;
        String albumName;

        public SendCommTask(String peer, String albumName, ListPhotosActivity listPhotosActivity) {
            this.peer = peer;
            this.albumName = albumName;
            this.context = listPhotosActivity;

        }

        @Override
        protected ArrayList<PhotoToSend> doInBackground(String... msg) {
            try {
                mCliSocket = new SimWifiP2pSocket(peer,
                        Integer.parseInt(context.getString(R.string.port)));

                ObjectOutputStream out = new ObjectOutputStream(mCliSocket.getOutputStream());

                out.writeObject(albumName);

                ObjectInputStream in = new ObjectInputStream(mCliSocket.getInputStream());

                photos = (ArrayList<PhotoToSend>)in.readObject();

                Log.d("sizeofphotos",photos.size() + "");

                mCliSocket.close();

                return photos;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            mCliSocket = null;
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<PhotoToSend> result) {
            context.addPhotos(result);


        }
    }

    public void unregisterReceiver() {
        context.unregisterReceiver(mReceiver);
    }

}
