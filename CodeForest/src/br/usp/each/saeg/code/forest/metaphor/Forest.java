package br.usp.each.saeg.code.forest.metaphor;

import java.util.*;

import javax.media.j3d.*;

import br.usp.each.saeg.code.forest.metaphor.assembler.*;

public interface Forest {

    void applyForestRestrictions();
    List<TransformGroup> getTrees();
    float getZ();
    float getX();
    float getY();
    List<Trunk> getTrunks();
    ForestRestrictions getRestrictions();

}