/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.jetpack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cd4017be.lib.ConfigurationFile;

/**
 *
 * @author CD4017BE
 */
public class JetPackConfig 
{
    private static final Mode DefaultMode = new Mode("Default", 2.0F, 0.1F, 0.0F, 0.0F, true);
    private static final Mode FlightMode = new Mode("Flight", 1.0F, 0.1F, 90F, 1.0F, true);
    public static int mode = 0;
    public static Mode[] activeModes = {DefaultMode, FlightMode};
    public static ArrayList<Mode> allModes = new ArrayList<Mode>();
    
    public static class Mode {
        public String name;
        public boolean active;
        public float vertAngleOffset;
        public float vertAngleFaktor;
        public float moveStrength;
        public float verticalComp;
        
        public Mode(String name, float vert, float acc, float offset, float factor, boolean active)
        {
            this.name = name;
            this.verticalComp = vert;
            this.moveStrength = acc;
            this.vertAngleOffset = offset;
            this.vertAngleFaktor = factor;
            this.active = active;
        }
        
        public Mode()
        {
        	name = "";
        }
        
        public Mode(DataInputStream dis) throws IOException
        {
            this.name = dis.readUTF();
            this.active = dis.readBoolean();
            this.verticalComp = dis.readFloat();
            this.moveStrength = dis.readFloat();
            this.vertAngleOffset = dis.readFloat();
            this.vertAngleFaktor = dis.readFloat();
        }
    }
    
    private static File file;
    
    public static void loadData()
    {
        file = new File(ConfigurationFile.configDir, "Jetpack.dat");
        allModes.clear();
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            int n = dis.readShort();
            for (int i = 0; i < n; i++) {
                allModes.add(new Mode(dis));
            }
            dis.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
        saveData();
    }
    
    public static void saveData()
    {
        if (allModes.isEmpty()) {
        	allModes.add(DefaultMode);
        	allModes.add(FlightMode);
        }
        ArrayList<Mode> act = new ArrayList<Mode>();
        for (Mode m : allModes) {
            if (m.active) act.add(m);
        }
        if (act.isEmpty()) act.add(DefaultMode);
        activeModes = act.toArray(new Mode[act.size()]);
        
        try {
            file.createNewFile();
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
            dos.writeShort(allModes.size());
            for (Mode m : allModes) {
                dos.writeUTF(m.name);
                dos.writeBoolean(m.active);
                dos.writeFloat(m.verticalComp);
                dos.writeFloat(m.moveStrength);
                dos.writeFloat(m.vertAngleOffset);
                dos.writeFloat(m.vertAngleFaktor);
            }
            dos.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    public static Mode getMode()
    {
        mode = mode % activeModes.length;
        return activeModes[mode];
    }
    
}
