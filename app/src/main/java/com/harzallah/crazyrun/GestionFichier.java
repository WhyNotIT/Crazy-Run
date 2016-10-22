package com.harzallah.crazyrun;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class GestionFichier {

    private static BufferedReader br;
    private static BufferedWriter bw;
    //private static String mFichierPath;
    private static String mTemp;
    //private File mFichier;
    private static Context context;

    public GestionFichier(Context context)
    {
        this.context=context;
        mTemp=context.getFilesDir()+"/TMP";
        //mFichierPath=context.getFilesDir()+"/"+nomFichier;
        //mFichier = new File(mFichierPath);
    }

    public boolean existe (String nomFichier)
    {
        File mFichier = new File(context.getFilesDir()+"/"+nomFichier);
        if (mFichier.exists())
            return true;
            else
        return false;
    }

    public boolean creeFichier (String nomFichier) {

        File mFichier = new File(context.getFilesDir()+"/"+nomFichier);

        try {
            mFichier.createNewFile();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public int updateValeur (String nomFichier, String mChamp, String val) {

        File mFichier = new File(context.getFilesDir()+"/"+nomFichier);

        int resultat=-1;
        String ch;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(mFichier), "UTF-8"));
            bw  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mTemp)));
            while (br.ready()) {
                ch = br.readLine();
                if (ch.contains(mChamp)) {
                    resultat=0;
                    bw.write(mChamp+val+"\n");
                } else {
                    bw.write(ch+"\n");
                }
            }
            bw.close();
            br.close();

            if (resultat==-1)
            {
                bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mFichier,true)));
                bw.write(mChamp+val+"\n");
                resultat=1;
            }
            else
            {
                bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mFichier)));
                br=new BufferedReader(new InputStreamReader(new FileInputStream(mTemp), "UTF-8"));
                while (br.ready())
                {
                    bw.write(br.readLine()+"\n");
                }
            }

            bw.close();
            br.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resultat;
    }

    public String selectValeur (String nomFichier, String mChamp)
    {
        File mFichier = new File(context.getFilesDir()+"/"+nomFichier);

        String valeur="";
        boolean trouvee=false;
        try {
        br = new BufferedReader(new InputStreamReader(new FileInputStream(mFichier), "UTF-8"));
            while (!trouvee && br.ready())
            {
                valeur=br.readLine();
                if (valeur.contains(mChamp))
                {
                    trouvee=true;
                    valeur=valeur.replace(mChamp,"");
                }
            }

            br.close();

            if (!trouvee)
            {
                bw  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mFichier,true)));
                bw.write(mChamp+"VIDE!\n");
                bw.close();
                valeur="VIDE!";
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return valeur;
    }

    public static List<String> getAllLines (String nomFichier)
    {
        List<String> l = new ArrayList<>();
        File f = new File(context.getFilesDir()+"/"+nomFichier);
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            while (br.ready())
            {
                l.add(br.readLine());
            }
            br.close();
        }catch (IOException e){

        }


        return l;
    }

    public static boolean writeLine (String nomFichier, String line)
    {
        File f = new File(context.getFilesDir()+"/"+nomFichier);
        try {
             bw  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f,true)));
            bw.write(line+"\n");
            bw.close();
        }catch (IOException e){
            return false;
        }
        return true;

    }
}
