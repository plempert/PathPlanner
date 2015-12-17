package project6;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class MapToPlan {
    int SIZE;
    int [][] occupancyGrid;
    int pLength;
    double [] plan;
    Point2D<Double> startPosition;
    Point2D<Double> finishPosition;
    Point2D<Integer> startPositionGrid;
    Point2D<Integer> finishPositionGrid;
    double m;
    double b;

    // Point2D class can support Doubles and Integers
    // *Perhaps two separate classes or just an integer class would do?
    public static class Point2D<T>{
        T x;
        T y;
        Point2D(T x, T y){
            this.x = x;
            this.y = y;
        }
        public String toString(){
            return "("+x+","+y+")";
        }
        public int compareTo(Point2D<T> p){
            return (x==p.x && y==p.y ? 0 : -1);
        }
    }

    public boolean areAdjacent(Point2D<Integer> a, Point2D<Integer> b){
        int differenceInX = Math.abs(a.x-b.x);
        int differenceInY = Math.abs(a.y-b.y);
        if(differenceInX==1 && differenceInY==1) return true;
        if(differenceInX==1 && differenceInY==0) return true;
        if(differenceInX==0 && differenceInY==1) return true;
        return false;
    }

    // A comparator for the priority queue
    public class Point2DIntegerComparator implements Comparator<Point2D<Integer>> {
        @Override
        public int compare(Point2D<Integer> a, Point2D<Integer> b) {
            if (manhattan(a) < manhattan(b)) return -1;
            if (manhattan(a) > manhattan(b)) return 1;
            else return 0;
        }
    }

    // Returns the manhattan distance to the finish position.
    public int manhattan(Point2D<Integer> p){
        return (Math.abs(p.x-finishPositionGrid.x)+Math.abs(p.y-finishPositionGrid.y));
    }

    public static void main(String [] args){
        MapToPlan mapToPlan = new MapToPlan();
        mapToPlan.printMap();
        //System.out.println(mapToPlan.finishPositionGrid.toString());
        mapToPlan.createPlan();
        //System.out.println(mapToPlan.isAnObstacle(new MapToPlan.Point2D<Integer>(3,1)));
        //System.out.println(mapToPlan.startPosition.toString());
        System.out.println("test");
    }

    // Constructor
    // Initialize member variables here.
    MapToPlan(){
        SIZE = 32;

        startPosition = new Point2D<Double>(-6.5,-6.5);
        finishPosition = new Point2D<Double>(7.0,7.0);
        m = 2;
        b = 16;
        startPositionGrid = convertToPoint2DInteger(startPosition);
        finishPositionGrid = convertToPoint2DInteger(finishPosition);
        // For testing...
        //SIZE = 6;
        occupancyGrid = new int [SIZE][SIZE];
        readMap("/Users/Patrick/IdeaProjects/Project6/src/project6/map.txt");
        //startPositionGrid = new Point2D<Integer>(3,3);
        //finishPositionGrid = new Point2D<Integer>(3,5);
    }

    public Point2D<Integer> convertToPoint2DInteger(Point2D<Double> d){
        return new Point2D<Integer>((int)Math.floor(m*d.x+b),(int)Math.floor(m*d.y+b));
    }

    public ArrayList<Point2D<Integer>> getAdjacentElementsTo(Point2D<Integer> p){
        ArrayList<Point2D<Integer>> l = new ArrayList<Point2D<Integer>>();
        l.add(new Point2D<Integer>(p.x-1,p.y+1));
        l.add(new Point2D<Integer>(p.x,p.y+1));
        l.add(new Point2D<Integer>(p.x+1,p.y+1));
        l.add(new Point2D<Integer>(p.x-1,p.y));
        l.add(new Point2D<Integer>(p.x+1,p.y));
        l.add(new Point2D<Integer>(p.x-1,p.y-1));
        l.add(new Point2D<Integer>(p.x,p.y-1));
        l.add(new Point2D<Integer>(p.x+1,p.y-1));
        return l;
    }

    public boolean isAnObstacle(Point2D<Integer> i){
        if(i.x<0 || i.x>=SIZE || i.y<0 || i.y>=SIZE) return true;
        else return (occupancyGrid[i.x][i.y] == 1);
    }

    public void addAdjacentElementsToPQueue(PriorityQueue<Point2D<Integer>> open, Point2D<Integer> point, ArrayList<Point2D<Integer>> closed){
        ArrayList<Point2D<Integer>> adjacentElements = getAdjacentElementsTo(point);
        boolean containedInOpen;
        boolean containedInClosed;
        for(Point2D<Integer> i:adjacentElements){
            containedInOpen = false;
            containedInClosed = false;
            for(Point2D<Integer> j:open) if(i.compareTo(j)==0) containedInOpen = true;
            for(Point2D<Integer> j:closed) if(i.compareTo(j)==0) containedInClosed = true;
            if(!isAnObstacle(i) && !containedInOpen && !containedInClosed) open.add(i);
        }
    }


    static void sleep(double x){
        try {
            TimeUnit.MILLISECONDS.sleep((int)(x*1000));
        } catch (InterruptedException e) {
            //Handle exception
        }
    }

    // Work on this

    public ArrayList<Point2D<Double>> AStarAlgorithm(Point2D<Integer> startPositionGrid,Point2D<Integer> finishPositionGrid){
        Comparator<Point2D<Integer>> comparator = new Point2DIntegerComparator();
        PriorityQueue<Point2D<Integer>> open = new
                PriorityQueue<Point2D<Integer>>(comparator);
        ArrayList<Point2D<Integer>> closed = new
                ArrayList<Point2D<Integer>>();

        open.add(startPositionGrid);
        Point2D<Integer> currentWaypoint = open.peek();
        while(currentWaypoint.compareTo(finishPositionGrid)!=0) {
            closed.add(open.remove());
            addAdjacentElementsToPQueue(open, currentWaypoint, closed);
            currentWaypoint = open.peek();
            //System.out.println(open.peek().toString());
            //sleep(0.5);
        }
        closed.add(currentWaypoint);

        // Prune path
        boolean pathNeedsToBePruned = true;
        while(pathNeedsToBePruned){
            pathNeedsToBePruned = false;
            for(int i=0; i < closed.size()-2; i++){
                for(int j = closed.size()-1; j > i+1; j--){
                    if(areAdjacent(closed.get(i),closed.get(j))){
                        System.out.println(closed.get(i).toString() +" and "+closed.get(j).toString()+" are adjacent.");
                        sleep(0.1);
                        pathNeedsToBePruned = true;
                        for(int k = i+1; k<j; k++){
                            closed.remove(i+1);
                        }
                    }
                    if(pathNeedsToBePruned) break;
                }
                if(pathNeedsToBePruned) break;
//                if(areAdjacent(closed.get(i),closed.get(i+2))){
//                    closed.remove(i+1);
//                    pathNeedsToBePruned = true;
//                    break;
//                }
            }
        }

        for(Point2D<Integer> i:closed){
            System.out.println(i.toString());
        }

        for(Point2D<Integer> i:closed){
            occupancyGrid[i.x][i.y] = closed.indexOf(i)+1;
        }

        for(int j = SIZE-1; j>=0; j--) {
            for (int i = 0; i < SIZE; i++) {
                System.out.print(occupancyGrid[i][j]+" ");
            }
            System.out.println();
        }


        System.out.println("A* algorithm completed.");
        return new ArrayList<Point2D<Double>>();
    }

    public void createPlan(){
        ArrayList< Point2D<Double> > path = AStarAlgorithm(startPositionGrid,
                finishPositionGrid);



    }







    // Avoid below for now

    void readMap(String inputFile){
        Scanner scanner;
        try {
            scanner = new Scanner(new File(inputFile));
            for(int i = SIZE-1; i>=0; i--){
                for(int j = 0; j<SIZE; j++){
                    occupancyGrid[i][j] = scanner.nextInt();
                }
            }
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open map file.");
        }
        int [][] adjustedOccupancyGrid = new int[SIZE][SIZE];
        for(int i = 0; i<SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                adjustedOccupancyGrid[i][j] = occupancyGrid[j][i];
            }
        }
        occupancyGrid = adjustedOccupancyGrid;
    }
    void writeMap(){

    }
    void printMap(){
        for(int j = SIZE-1; j>=0; j--) {
            for (int i = 0; i < SIZE; i++) {
                System.out.print(occupancyGrid[i][j]+" ");
            }
            System.out.println();
        }
    }
    int  readPlanLength(){
        return 0;
    }
    void readPlan(){

    }
    void printPlan(){

    }
    void writePlan(){

    }
}