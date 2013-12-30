package pipe.models.component;

import parser.ExprEvaluator;
import pipe.exceptions.TokenLockedException;
import pipe.gui.ApplicationSettings;
import pipe.models.visitor.PetriNetComponentVisitor;
import pipe.utilities.math.Matrix;
import pipe.views.*;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Token extends AbstractPetriNetComponent {

    @Pnml("id")
    private String id;

    @Pnml("enabled")
    private boolean enabled;

    private int currentMarking;
    private int lockCount = 0; // So that users cannot change this class while
    // places are marked with it

    @Pnml("color")
    private Color color;
    private Matrix incidenceMatrix;
    // weight of connections from transition to place for given token
    private Matrix forwardsIncidenceMatrix;
    // weight of connections from place to transition for given token
    private Matrix backwardsIncidenceMatrix;
    private Matrix inhibitionMatrix;

    public Token() {
        this("", false, 0, Color.BLACK);
    }

    public Token(String id, boolean enabled, int currentMarking, Color color) {
        this.id = id;
        this.enabled = enabled;
        this.currentMarking = currentMarking;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setName(String name) {
        setId(name);
    }

    public int getCurrentMarking() {
        return currentMarking;
    }

    public void setCurrentMarking(int currentMarking) {
        this.currentMarking = currentMarking;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     * @throws TokenLockedException if the Token is locked
     */
    public void setEnabled(boolean enabled) throws TokenLockedException {
        if (!isLocked()) {
            this.enabled = enabled;
        } else {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("TokenSetController.updateOrAddTokenView: Enabled TokenView is in use for ")
                    .append(getLockCount())
                    .append(" Places.  It may not be disabled unless tokens are removed from those Places.\n")
                    .append("Details: ")
                    .append(this.toString());

            throw new TokenLockedException(messageBuilder.toString());
        }
    }

    @Override
    public String toString() {
        return getId();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void incrementLock() {
        lockCount++;
    }

    public void decrementLock() {
        lockCount--;
    }

    public boolean isLocked() {
        return lockCount > 0;
    }

    public int getLockCount() {
        return lockCount;
    }

    public void setLockCount(int newLockCount) {
        lockCount = newLockCount;
    }

    public Matrix getIncidenceMatrix() {
        return incidenceMatrix;
    }

    public int[][] getIncidenceMatrix(Collection<Arc> arcs, Collection<Transition> transitions, Collection<Place> places) {
        if (incidenceMatrix == null || incidenceMatrix.matrixChanged) {
            createIncidenceMatrix(arcs, transitions, places);
        }
        return (incidenceMatrix != null ? incidenceMatrix.getArrayCopy() : null);
    }

    // New method for TransModel.java
    public int[][] simpleMatrix() {
        return incidenceMatrix.getArrayCopy();
    }

    public void setIncidenceMatrix(Matrix incidenceMatrix) {
        this.incidenceMatrix = incidenceMatrix;
    }

    public void createIncidenceMatrix(Collection<Arc> arcs, Collection<Transition> transitions, Collection<Place> places) {
        createForwardIncidenceMatrix(arcs, transitions, places);
        createBackwardsIncidenceMatrix(arcs, transitions, places);
        incidenceMatrix = new Matrix(forwardsIncidenceMatrix);
        incidenceMatrix = incidenceMatrix.minus(backwardsIncidenceMatrix);
        incidenceMatrix.matrixChanged = false;
    }

    public Matrix getForwardsIncidenceMatrix() {
        return forwardsIncidenceMatrix;
    }

    public int[][] getForwardsIncidenceMatrix(Collection<Arc> arcsArray,
                                              Collection<Transition> transitionsArray, Collection<Place> placesArray) {
        if (forwardsIncidenceMatrix == null
                || forwardsIncidenceMatrix.matrixChanged) {
            createForwardIncidenceMatrix(arcsArray, transitionsArray,
                    placesArray);
        }
        return (forwardsIncidenceMatrix != null ? forwardsIncidenceMatrix
                .getArrayCopy() : null);
    }

    public int[][] simpleForwardsIncidenceMatrix() {
        return forwardsIncidenceMatrix.getArrayCopy();
    }

    public void setForwardsIncidenceMatrix(Matrix forwardsIncidenceMatrix) {
        this.forwardsIncidenceMatrix = forwardsIncidenceMatrix;
    }

    public Matrix getBackwardsIncidenceMatrix() {
        return backwardsIncidenceMatrix;
    }

    public int[][] getBackwardsIncidenceMatrix(Collection<Arc> arcsArray,
                                               Collection<Transition> transitionsArray, Collection<Place> placesArray) {
        if (backwardsIncidenceMatrix == null
                || backwardsIncidenceMatrix.matrixChanged) {
            createBackwardsIncidenceMatrix(arcsArray, transitionsArray,
                    placesArray);
        }
        return (backwardsIncidenceMatrix != null ? backwardsIncidenceMatrix
                .getArrayCopy() : null);
    }

    public int[][] simpleBackwardsIncidenceMatrix() {
        return backwardsIncidenceMatrix.getArrayCopy();
    }

    public void setBackwardsIncidenceMatrix(Matrix backwardsIncidenceMatrix) {
        this.backwardsIncidenceMatrix = backwardsIncidenceMatrix;
    }

    public Matrix getInhibitionMatrix() {
        return inhibitionMatrix;
    }

    public int[][] getInhibitionMatrix(Collection<InhibitorArcView> inhibitorArrayView,
                                       Collection<TransitionView> transitionsArray, Collection<PlaceView> placesArray) {
        if (inhibitionMatrix == null || inhibitionMatrix.matrixChanged) {
            createInhibitionMatrix(inhibitorArrayView, transitionsArray,
                    placesArray);
        }
        return (inhibitionMatrix != null ? inhibitionMatrix.getArrayCopy()
                : null);
    }

    public void setInhibitionMatrix(Matrix inhibitionMatrix) {
        this.inhibitionMatrix = inhibitionMatrix;
    }

    void createForwardIncidenceMatrix(Collection<Arc> arcs, Collection<Transition> transitions, Collection<Place> places) {
        throw new RuntimeException("Using old forwards incidence matrix method");
//        int placeSize = places.size();
//        int transitionSize = transitions.size();
//
//        forwardsIncidenceMatrix = new Matrix(placeSize, transitionSize);
//        for (Arc arc : arcs) {
//            Connectable target = arc.getTarget();
//            Connectable source = arc.getSource();
//
//            if (target instanceof Place) {
//                Place place = (Place) target;
//                if (source instanceof Transition) {
//                    Transition transition = (Transition) source;
//
//                    //TODO: Broken transitions
//                    String expression = arc.getWeightForToken(this);
//
//                    //TODO: PASS PETRI NET!!! VERY IMPORTANT
//                    ExprEvaluator paser = new ExprEvaluator(null);
//
//                    Integer weight = paser.parseAndEvalExpr(expression, id);
//                    if (weight == 0) {  // Ie at least one token to pass
//                        weight = 1;
//                    }
//
//                    if (forwardsIncidenceMatrix.get(transition) == null) {
//                        HashMap<Place, Integer> weights = new HashMap<Place, Integer>();
//                        forwardsIncidenceMatrix.put(transition, weights);
//                    }
//                    forwardsIncidenceMatrix.get(transition).put(place, weight);
//                }
//            }
//        }
    }

    /**
     * Creates Backwards Incidence Matrix from current Petri-Net
     *
     * @param arcs
     * @param transitions
     * @param places
     */
    void createBackwardsIncidenceMatrix(Collection<Arc> arcs, Collection<Transition> transitions, Collection<Place> places) {// Matthew
        throw new RuntimeException("Using old backwards incidence matrix method");
//        backwardsIncidenceMatrix.clear();
//
//        for (Arc arc : arcs) {
//            Connectable target = arc.getTarget();
//            Connectable source = arc.getSource();
//            if (target instanceof Transition)
//            {
//                Transition transition = (Transition) target;
//                if (source instanceof Place) {
//                    Place place = (Place) source;
//                    int enablingDegree = transition.isInfiniteServer() ? : 0;
//                }
//            }
//            if (pn instanceof PlaceView) {
//                PlaceView placeView = (PlaceView) pn;
//                pn = arcView.getTarget();
//                if (pn != null) {
//                    if (pn instanceof TransitionView) {
//                        TransitionView transitionView = (TransitionView) pn;
//                        boolean isTransitionInfiniteServer = transitionView.isInfiniteServer();
//                        int enablingDegree = 1;
//                        if (isTransitionInfiniteServer) {
//                            enablingDegree = ApplicationSettings.getApplicationView().getCurrentPetriNetView().getEnablingDegree(transitionView);
//                        }
//                        //TODO: Broken this
//                        int transitionNo = 0; //transitionsArray.indexOf(transitionView);
//                        int placeNo = 0; //placesArray.indexOf(placeView);
//                        List<MarkingView> markings = arcView.getWeight();
//                        for (MarkingView token : markings) {
//                            if (token.getToken().getID().equals(id)) {
//                                try {
//                                    int marking = token.getCurrentMarking();
//                                    if (marking == 0) {
//                                        marking = 1;
//                                    }
//                                    if (isTransitionInfiniteServer) {
//
//                                        backwardsIncidenceMatrix.set(
//                                                placeNo, transitionNo,
//                                                marking * enablingDegree);
//                                    } else {
//                                        backwardsIncidenceMatrix.set(
//                                                placeNo, transitionNo,
//                                                marking);//arcView.getWeightOfTokenClass(id));
//                                    }
//                                    //	System.out.println("compare: "+ token.getCurrentMarking()+ " raw: "+ token.getCurrentFunctionalMarking()+"   "+arcView.getWeightFunctionOfTokenClass(id));
//
//                                    //     System.out.println(arcView.getWeightFunctionOfTokenClass(id));
//                                } catch (Exception e) {
//                                    JOptionPane.showMessageDialog(null, "Problem in backwardsIncidenceMatrix");
//                                    System.out.println("p:" + placeNo + ";t:" + transitionNo + ";w:" + arcView.getWeight());
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    public void createInhibitionMatrix(Collection<InhibitorArcView> inhibitorsArray, Collection<TransitionView> transitionsArray, Collection<PlaceView> placesArray) {
        int placeSize = placesArray.size();
        int transitionSize = transitionsArray.size();
        inhibitionMatrix = new Matrix(placeSize, transitionSize);

        for (InhibitorArcView inhibitorArcView : inhibitorsArray) {
            if (inhibitorArcView != null) {
                PetriNetViewComponent pn = inhibitorArcView.getSource();
                if (pn != null) {
                    if (pn instanceof PlaceView) {
                        PlaceView placeView = (PlaceView) pn;
                        pn = inhibitorArcView.getTarget();
                        if (pn != null) {
                            if (pn instanceof TransitionView) {
                                TransitionView transitionView = (TransitionView) pn;
                                //TODO: Broken this
                                int transitionNo = 0; //transitionsArray.indexOf(transitionView);
                                int placeNo = 0; //placesArray.indexOf(placeView);
                                try {
                                    inhibitionMatrix.set(placeNo, transitionNo,
                                            1);
                                } catch (Exception e) {
                                    JOptionPane.showMessageDialog(null,
                                            "Problema a inhibitionMatrix");
                                    System.out.println("p:" + placeNo + ";t:"
                                            + transitionNo + ";w:"
                                            + inhibitorArcView.getWeight());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    public void accept(PetriNetComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Token token = (Token) o;

        if (!color.equals(token.color)) {
            return false;
        }
        if (!id.equals(token.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + color.hashCode();
        return result;
    }
}
