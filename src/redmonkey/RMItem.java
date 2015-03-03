/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package redmonkey;

import com.jme3.math.Vector3f;
import java.util.ArrayList;

/**
 *
 */
public class RMItem {
    public Vector3f position;
    public Vector3f direction;
    public ArrayList<String> tags=new ArrayList<String>();
    
    public RMItem(){
    }
    
    public RMItem(Vector3f position, String... newTags){
        this.position=position;
        for (String tag:newTags)
            this.tags.add(tag);
    }
    
    public boolean hasTag(String tag){
        return tags.contains(tag);
    }
    
    public String toString(){
        String s="";
        for (String tag:tags)
            s+=(tag+",");
        s+="["+position.x+","+position.y+","+position.z+"]";
        return s;
    }
}