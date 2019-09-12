package co.scalium.scabase;

import android.content.Context;
import android.content.SharedPreferences;

public class FilePG {

    SharedPreferences parsa ;
    Context context;

    public FilePG(String parsa, Context context) {
        this.parsa = context.getSharedPreferences(parsa,0);
        this.context = context;
    }

    // setters
    public void fileSet(String name,String value)
    {
        this.parsa.edit().putString(name,value).apply();
    }
    public void fileSet(String name,boolean value)
    {
        this.parsa.edit().putBoolean(name,value).apply();
    }
    public void fileSet(String name,int value)
    {
        this.parsa.edit().putInt(name,value).apply();
    }
    // getters
    public String fileGetString(String name)
    {
        return  this.parsa.getString(name,"NOT_SET");
    }
    public boolean fileGetBoolean(String name)
    {
        return  this.parsa.getBoolean(name,false);
    }
    public int fileGetInt(String name)
    {
        return  this.parsa.getInt(name,0);
    }

}

