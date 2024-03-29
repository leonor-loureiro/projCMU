package pt.ulisboa.tecnico.cmov.p2photo.wifidirect;

import android.content.ComponentName;
import android.content.Context;
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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

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
import pt.ulisboa.tecnico.cmov.p2photo.storage.MemoryCacheManager;

public class  WifiDirectManager {

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

    /**
     * initiate WifiDirect
     */

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
        new GetPhotosCommTask(virtIp,username,albumName,listPhotosActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Creates the new set of members available in the wifi direct group
     * @param devices devices in group.
     */
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices, SimWifiP2pInfo groupInfo) {
        // compile list of network members
        this.globalVariables.setMembersInGroup(new ArrayList<Member>());
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);

            Log.d(TAG,"addingdevice" + device.getVirtIp());
            addDeviceName(device.getVirtIp(),deviceName);

        }

        Log.d(TAG,"size of members" + this.globalVariables.getMembersInGroup().size() + " should be " + groupInfo.getDevicesInNetwork().size());

        // display list of network members
     /*   new AlertDialog.Builder(context)
                .setTitle("Devices in WiFi Network")
                .setMessage(peersStr.toString())
                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show(); */


    }

    /**
     * Asks the device for his user name.
     * @param virtIp the virtual ip where the user is.
     */
    private void addDeviceName(String virtIp,String deviceName) {
        Log.d(TAG,"adding user with devicename" + deviceName + virtIp);
        new GetUserNameCommTask(virtIp,deviceName).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }

    /**
     * Binds the service
     */
    public void bindService() {
        Intent intent = new Intent(context, SimWifiP2pService.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
    }

    /**
     * unregisters the receiver, unbins the connection and closes the serversocket.
     */
    public void unbindService(){
        if(mBound) {
            try {
                unregisterReceiver();
                context.unbindService(mConnection);
            }catch (IllegalArgumentException e){
                Log.e(TAG, e.getMessage());
            }
            try {
                mSrvSocket.close();
                mSrvSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mBound = false;

    }




    /**
     * Class used to receive messages from other devices.
     */
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
                    if(mSrvSocket == null){
                        return null;
                    }
                    SimWifiP2pSocket sock = mSrvSocket.accept();

                    ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

                    Object value = in.readObject();

                    try {
                        String request =  (String) value;
                        Log.d(TAG,"Request: " + request);
                        //if its an "?", it means the user wants to know the user name.
                        if (request.equals("?")) {

                            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                            Log.d(TAG,"writing the username to " + globalVariables.getUser().getName());
                            out.writeObject(globalVariables.getUser().getName());

                        //else it means the user wants to receive the photos from this device of a certain album
                        } else {
                            String album = (String) value;

                            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());


                            String fileID = globalVariables.getFileID(album);
                            Log.d(TAG,"fileID: " + fileID);


                            List<Photo> photos = new ArrayList<>();
                            //get the photos given a certain fileID
                            if ((fileID != null) && !(fileID.equals("null")))
                                photos = globalVariables.getFileManager().getAlbumPhotos(fileID);

                            ArrayList<PhotoToSend> photosToSend = new ArrayList<>();

                            //create a new list of objects PhotoToSend, because  bitmap needs to be encoded
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
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "IncommingCommTask: interrupted");
            return null;

        }
    }

    /**
     * class used to ask a device for its photos of a certain album
     */
    public class GetPhotosCommTask extends AsyncTask<String, String, ArrayList<PhotoToSend>> {

        /*
        context so we can update the UI
         */
        private final ListPhotosActivity context;
        private SimWifiP2pSocket mCliSocket = null;
        /*
        adress of the device
         */
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

            } catch (SocketException e){

                Log.d(TAG,"the other device is offline");
                MemoryCacheManager cacheManager = ((GlobalVariables) context.getApplicationContext()).getCacheManager();
                List<Photo> cachedPhotos = cacheManager.getAlbumPhotos(username, albumName, false);
                ArrayList<PhotoToSend> photosToSends = new ArrayList<>();
                for(Photo photo : cachedPhotos){

                    photosToSends.add(new PhotoToSend(photo.getUrl(),Utils.encodeBitmap(photo.getBitmap())));

                }

                return photosToSends;
            }catch (IOException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            mCliSocket = null;
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<PhotoToSend> result) {
            //update the UI
            context.addPhotos(username, albumName, result);


        }
    }

    /**
     * class used to ask a device for its name
     */
    public class GetUserNameCommTask extends AsyncTask<String, String,String> {

        private final String deviceName;
        private SimWifiP2pSocket mCliSocket = null;
        String peer;
        public GetUserNameCommTask(String peer, String deviceName) {
            this.peer = peer;
            this.deviceName = deviceName;

        }

        @Override
        protected String doInBackground(String... msg) {
            try {
                Log.d("TAG", "the peer is " + peer);
                mCliSocket = new SimWifiP2pSocket(peer,
                        Integer.parseInt(context.getString(R.string.port)));


                ObjectOutputStream out = new ObjectOutputStream(mCliSocket.getOutputStream());

                Log.d(TAG,"writing 0161 ");
                out.writeObject("?");

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

            //update the current members in group
            Log.d(TAG,"adding " + result + " to " + globalVariables.getUser().getName());
            globalVariables.getMembersInGroup().add(new Member(result,peer,deviceName));

        }
    }


    /*
    unregisters the broadcastreceiver
     */
    public void unregisterReceiver() {
        context.unregisterReceiver(mReceiver);
    }

}
