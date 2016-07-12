package com.jvilam.foundapet.entities;

import java.io.Serializable;
import java.util.Vector;

/**
 * Created by jvilam on 23/05/2016.
 *
 */
public class Pets implements Serializable{
    Vector<Pet> pets;

    public Pets(){
        pets = new Vector<>();
    }

    public void addPet(Pet p){
        pets.add(p);
    }

    public Pet getPet(int p){
        return pets.get(p);
    }

    public int size(){
        return pets.size();
    }

}
