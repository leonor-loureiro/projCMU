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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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


    public void send(String virtIp, String username, String albumName, ListPhotosActivity listPhotosActivity) {
        //new SendCommTask(virtIp, username, albumName,listPhotosActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new GetPhotosCommTask(virtIp,username,albumName,listPhotosActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {
        // compile list of network members
        StringBuilder peersStr = new StringBuilder();
        this.globalVariables.setMembersInGroup(new ArrayList<Member>());
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null)?"??":device.getVirtIp()) + ")\n";
            peersStr.append(devstr);

            /*if(!membersInGroup.contains(new Member(deviceName))) {
                membersInGroup.add(new Member(deviceName, device.getVirtIp(),"qlqrcoisa"));

            }*/
            Log.d(TAG,"addingdevice" + device.getVirtIp());
            addDeviceName(device.getVirtIp());

        }

        Log.d(TAG,"size of members" + this.globalVariables.getMembersInGroup().size() + " should be " + groupInfo.getDevicesInNetwork().size());

        // display list of network members
        new AlertDialog.Builder(context)
                .setTitle("Devices in WiFi Network")
                .setMessage(peersStr.toString())
                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();


    }

    private void addDeviceName(String virtIp) {
        new GetUserNameCommTask(virtIp).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }

    public void bindService() {
        Intent intent = new Intent(context, SimWifiP2pService.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
    }

    public void unbindService(){
        if(mBound) {
            unregisterReceiver();
            context.unbindService(mConnection);
            try {
                mSrvSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mBound = false;

    }



    public class IncommingCommTask extends AsyncTask<Void, String, Void> {


        Boolean isInt = true;

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

                    ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

                    Object value = in.readObject();

                    try {
                        int check = (int) value;
                        Log.d(TAG,"check if is int");
                        isInt = true;
                    } catch (ClassCastException e) {
                        Log.d(TAG, "setting isint to " + isInt + "");
                        isInt = false;
                    }

                    try {
                        Log.d(TAG,"value of isINT is " + isInt + "");
                        if (isInt) {

                            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                            Log.d(TAG,"writing the username to " + globalVariables.getUser().getName());
                            out.writeObject(globalVariables.getUser().getName());

                        } else {
                            String album = (String) value;

                            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());


                            String fileID = globalVariables.getFileID(album);
                            Log.d(TAG,"fileID: " + fileID);


                            List<Photo> photos = new ArrayList<>();
                            if ((fileID != null) && !(fileID.equals("null")))
                                photos = globalVariables.getFileManager().getAlbumPhotos(fileID);

                            ArrayList<PhotoToSend> photosToSend = new ArrayList<>();

                            for (Photo photo : photos) {
                                Log.d(TAG,"photo: " + photo.getUrl() + " mine = " + photo.getMine());
                                if (photo.isMine()) {
                                    photosToSend.add(new PhotoToSend(photo.getUrl(), Utils.encodeBitmap(photo.getBitmap())));
                                }
                            }
                            Log.d(TAG,"sending this number of photos " + photosToSend.size() + "");
                            out.writeObject(photosToSend);

                        }

                    } catch (IOException e) {
                        Log.d("Error reading socket:", e.getMessage());
                    } finally {
                        sock.close();
                    }
                } catch (IOException e) {
                    Log.d("Error socket:", e.getMessage());
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "IncommingCommTask: interrupted");
            return null;

        }
    }

    public class GetPhotosCommTask extends AsyncTask<String, String, ArrayList<PhotoToSend>> {

        private final ListPhotosActivity context;
        private SimWifiP2pSocket mCliSocket = null;
        String peer;
        String albumName;
        String username;

        public GetPhotosCommTask(String peer, String username, String albumName, ListPhotosActivity listPhotosActivity) {
            this.peer = peer;
            this.username = username;
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
            context.addPhotos(username, albumName, result);


        }
    }
    public class GetUserNameCommTask extends AsyncTask<String, String,String> {

        private SimWifiP2pSocket mCliSocket = null;
        String peer;
        public GetUserNameCommTask(String peer) {
            this.peer = peer;

        }

        @Override
        protected String doInBackground(String... msg) {
            try {
                Log.d("TAG", "the peer is " + peer);
                mCliSocket = new SimWifiP2pSocket(peer,
                        Integer.parseInt(context.getString(R.string.port)));


                ObjectOutputStream out = new ObjectOutputStream(mCliSocket.getOutputStream());

                Log.d(TAG,"writing 0161 ");
                out.writeObject(0161);

                ObjectInputStream in = new ObjectInputStream(mCliSocket.getInputStream());

                String name = (String) in.readObject();
                Log.d(TAG,"the name is " + name);

                mCliSocket.close();

                return name;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            mCliSocket = null;
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG,"adding " + result + " to " + globalVariables.getUser().getName());
            globalVariables.getMembersInGroup().add(new Member(result,peer,"qllrcoisa"));

        }
    }


    public void unregisterReceiver() {
        context.unregisterReceiver(mReceiver);
    }

}
