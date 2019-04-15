package pt.ulisboa.ist.cmu.p2photo.server.services;

import pt.ulisboa.ist.cmu.p2photo.server.data.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public interface AtomicFileManager {

    static String destFilename =  System.getProperty("user.dir") + "/userList";

    static List<User> getUserList() throws IOException, ClassNotFoundException {

        ObjectInputStream in = new ObjectInputStream(new FileInputStream(destFilename+".ser"));
        List<User> users = (List<User>) in.readObject();
        in.close();

        return users;
    }

    static void atomicFileMove(String sourceFile, String destinationFile)
            throws IOException {
        Path sourcePath = Paths.get(sourceFile);
        Path destinationPath = Paths.get(destinationFile);
        Files.move(sourcePath, destinationPath, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Stores in an atomic way a list of serializable objects
     * @param content the List to be serialized
     * @throws IOException
     * @throws ClassNotFoundException
     */
    static void atomicWriteObjectToFile(List<?> content)
            throws IOException, ClassNotFoundException {

        //Temp file
        File tempFile = null;

        //For the write
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        //For the read
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {

            //Create a tmp file
            // Uses resource directory to avoid issues with multiple drives
            tempFile = File.createTempFile(destFilename + "-", ".tmp",  new File(System.getProperty("user.dir")));

            fos = new FileOutputStream(tempFile.getAbsolutePath());
            oos = new ObjectOutputStream(fos);

            //Write object to file output stream
            oos.writeObject(content);

            //Flush the data from the stream into the buffer
            fos.flush();

            //Commit data to disk
            fos.getFD().sync();

            //Close buffer
            fos.close();

            //Confirm the data was successfully saved to disk (not sure if necessary)
            fis = new FileInputStream(tempFile.getAbsolutePath());
            ois = new ObjectInputStream(fis);
            List<User> arrayList = (List) ois.readObject();

            fis.close();

            //if(arrayList.equals(content)){
            //Data was successfully written to temp file
            //replace destinationFile with tempFile
            atomicFileMove(tempFile.getAbsolutePath(), destFilename + ".ser");

            //}

        }finally {
            //Close output streams/buffers
            if(oos != null)
                oos.close();
            if(fos != null)
                fos.close();

            //Close input streams//buffers
            if(ois != null)
                ois.close();
            if(fis != null)
                fis.close();

            //Delete temporary file, if it was created
            if(tempFile != null && tempFile.exists())
                tempFile.delete();
        }

    }


}