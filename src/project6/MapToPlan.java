package project6;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MapToPlan {
    int WAYPOINT = -999;
    int SIZE;
    int [][] occupancyGrid;
    int [][] costGrid;
    int pLength;
    double [] plan;
    P2dD startPosition;
    P2dD finishPosition;
    P2dI startPositionGrid;
    P2dI finishPositionGrid;
    double m;
    double b;

    public static void main(String [] args){
        MapToPlan mapToPlan = new MapToPlan();
    }

    // Constructor
    // Initialize member variables here.
    MapToPlan(){
        SIZE = 32;

        startPosition = new P2dD(-6.5,-6.5);
        finishPosition = new P2dD(7.0,7.0);
        m = 2;
        b = 16;
        startPositionGrid = convertToPoint2DInteger(startPosition);
        finishPositionGrid = convertToPoint2DInteger(finishPosition);

        occupancyGrid = new int [SIZE][SIZE];
        costGrid = new int [SIZE][SIZE];
        readMap(System.getProperty("user.dir")+"/src/project6/map.txt");
        generateCostGrid();
        printMap(occupancyGrid);
        printMap(costGrid);
        createPlan();

    }

    void generateCostGrid(){
        for(int i=0; i<SIZE; i++){
            for(int j=0; j<SIZE; j++){
                if(occupancyGrid[i][j]==1) costGrid[i][j] = -1;
                else costGrid[i][j] = -2;
            }
        }

        costGrid[finishPositionGrid.x][finishPositionGrid.y] = 0;
        LinkedList<P2dI> initializationQueue = new LinkedList<P2dI>();
        initializationQueue.add(finishPositionGrid);
        while(!initializationQueue.isEmpty()){
            P2dI cur = initializationQueue.removeFirst();
            ArrayList<P2dI> adjList = getAdjacentElementsTo(cur);
            for(P2dI adjPoint : adjList){
                if(!isAnObstacle(adjPoint)) {
                    if (costGrid[adjPoint.x][adjPoint.y] == -2) {
                        costGrid[adjPoint.x][adjPoint.y] = costGrid[cur.x][cur.y] + 1;
                        initializationQueue.add(adjPoint);
                    }
                }
            }
        }


    }

    public void println(String s){
        System.out.println(s);
    }

    public void println(){
        System.out.println();
    }
    
    public static class P2dI extends Point2D<Integer>{
        P2dI(int x, int y){
            super(x,y);
        }

        public boolean isAdjacentTo(P2dI a){
            int differenceInX = Math.abs(a.x-x);
            int differenceInY = Math.abs(a.y-y);
            if(differenceInX==1 && differenceInY==1) return true;
            if(differenceInX==1 && differenceInY==0) return true;
            if(differenceInX==0 && differenceInY==1) return true;
            return false;
        }

        public int manhattanDistanceTo(P2dI a){
            return Math.abs(x-a.x)+Math.abs(y-a.y);
        }

        public String directionTo(P2dI p){
            if(!this.isAdjacentTo(p)) return "undefined";
            else {
                if(p.x == x-1 && p.y == y+1) return "northwest";
                if(p.x == x   && p.y == y+1) return "north";
                if(p.x == x+1 && p.y == y+1) return "northeast";
                if(p.x == x+1 && p.y == y) return "east";
                if(p.x == x+1 && p.y == y-1) return "southeast";
                if(p.x == x   && p.y == y-1) return "south";
                if(p.x == x-1 && p.y == y-1) return "southwest";
                if(p.x == x-1 && p.y == y) return "west";
            }
            return "";
        }
    }

    // A comparator for the priority queue
    public class P2dIComparator implements Comparator<P2dI> {
        @Override
        public int compare(P2dI a, P2dI b) {
            if (costGrid[a.x][a.y] < costGrid[b.x][b.y]) return -1;
            if (costGrid[a.x][a.y] > costGrid[b.x][b.y]) return 1;
            else{
                double aToFinish = Math.sqrt(Math.pow(a.x-finishPositionGrid.x,2)+Math.pow(a.y-finishPositionGrid.y,2));
                double bToFinish = Math.sqrt(Math.pow(b.x-finishPositionGrid.x,2)+Math.pow(b.y-finishPositionGrid.y,2));
                if(aToFinish < bToFinish) return -1;
                if(aToFinish > bToFinish) return 1;
                else return 0;
            }
        }
    }

    public static class P2dD extends Point2D<Double>{
        P2dD(double x, double y){
            super(x,y);
        }
    }
    
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

    public P2dI convertToPoint2DInteger(P2dD d){
        return new P2dI((int)Math.floor(m*d.x+b),(int)Math.floor(m*d.y+b));
    }

    public P2dD convertToPoint2DDouble(P2dI i){
        return new P2dD((i.x-b)/m,(i.y-b)/m);
    }

    public ArrayList<P2dI> getAdjacentElementsTo(P2dI p){
        ArrayList<P2dI> l = new ArrayList<P2dI>();
        l.add(new P2dI(p.x-1,p.y+1));
        l.add(new P2dI(p.x,p.y+1));
        l.add(new P2dI(p.x+1,p.y+1));
        l.add(new P2dI(p.x-1,p.y));
        l.add(new P2dI(p.x+1,p.y));
        l.add(new P2dI(p.x-1,p.y-1));
        l.add(new P2dI(p.x,p.y-1));
        l.add(new P2dI(p.x+1,p.y-1));
        return l;
    }

    public boolean isAnObstacle(P2dI i){
        if(i.x<0 || i.x>=SIZE || i.y<0 || i.y>=SIZE) return true;
        else return (occupancyGrid[i.x][i.y] == 1);
    }
    
    public void addAdjacentElementsToPQueue(PriorityQueue<P2dI> pathCandidates, P2dI point, ArrayList<P2dI> path){
        ArrayList<P2dI> adjacentElements = getAdjacentElementsTo(point);
        boolean containedInOpen;
        boolean containedInClosed;
        for(P2dI i:adjacentElements){
            containedInOpen = false;
            containedInClosed = false;
            for(P2dI j:pathCandidates) if(i.compareTo(j)==0) containedInOpen = true;
            for(P2dI j:path) if(i.compareTo(j)==0) containedInClosed = true;
            if(!isAnObstacle(i) && !containedInOpen && !containedInClosed) pathCandidates.add(i);
        }
    }


//    static void sleep(double x){
//        try {
//            TimeUnit.MILLISECONDS.sleep((int)(x*1000));
//        } catch (InterruptedException e) {
//            //Handle exception
//        }
//    }


    public boolean isCollinear(P2dI a, P2dI b, P2dI c){
        return a.directionTo(b).equals(b.directionTo(c));
    }

    public ArrayList<P2dI> findPath(P2dI start, P2dI finish){
        ArrayList<P2dI> path = new ArrayList<P2dI>();
        PriorityQueue<P2dI> pathCandidates = new PriorityQueue<P2dI>(new P2dIComparator());
        pathCandidates.add(start);
        P2dI currentWaypoint = pathCandidates.remove();
        while(currentWaypoint.compareTo(finish)!=0) {
            path.add(currentWaypoint);
            addAdjacentElementsToPQueue(pathCandidates, currentWaypoint, path);
            currentWaypoint = pathCandidates.remove();
        }
        path.add(currentWaypoint);

        for(int i=0; i<path.size()-2; i++){
            if(path.get(i).manhattanDistanceTo(path.get(i+2))==2 &&
            !isCollinear(path.get(i),path.get(i+1),path.get(i+2))){
                path.remove(i+1);
                path.add(i+1,new P2dI((path.get(i).x+path.get(i+2).x)/2,(path.get(i).y+path.get(i+2).y)/2));
            }
        }

        return path;

    }

    public static int[][] deepCopyIntMatrix(int[][] input) {
        if (input == null)
            return null;
        int[][] result = new int[input.length][];
        for (int r = 0; r < input.length; r++) {
            result[r] = input[r].clone();
        }
        return result;
    }

    public void createPlan(){
        ArrayList<P2dI> path = findPath(startPositionGrid, finishPositionGrid);
        ArrayList<P2dI> waypoints = new ArrayList<P2dI>();
        String prevDirection = path.get(0).directionTo(path.get(1));
        String currDirection;

        for(int i = 1; i < path.size()-1; i++){
            currDirection = path.get(i).directionTo(path.get(i+1));
            if(!prevDirection.contentEquals(currDirection)){
                waypoints.add(path.get(i));
            }
            prevDirection = currDirection;
        }

        waypoints.add(finishPositionGrid);

        int [][] pathMap, waypointMap;
        pathMap = deepCopyIntMatrix(occupancyGrid);
        waypointMap = deepCopyIntMatrix(occupancyGrid);

        for(P2dI i:path) pathMap[i.x][i.y] = path.indexOf(i)+1;
        printMap(pathMap);
        for(P2dI i:waypoints) waypointMap[i.x][i.y] = WAYPOINT;
        printMap(waypointMap);

        pLength = waypoints.size()*2;
        plan = new double[pLength];
        P2dD doublePoint;
        for(int i = 0; i<pLength; i++){
            doublePoint = convertToPoint2DDouble(waypoints.get(i/2));
            if(i%2==0) plan[i] = doublePoint.x;
            if(i%2==1) plan[i] = doublePoint.y;
        }

        String planFile = pLength + " ";
        for(int i=0; i< pLength; i++){
            planFile += plan[i]+" ";
        }
        printPlan();
        writePlan(System.getProperty("user.dir")+"/src/project6/"+"plan-out.txt", planFile);
        writeMap(waypointMap, System.getProperty("user.dir")+"/src/project6/"+"map-out.txt");
    }

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
            println("Unable to open map file.");
        }
        int [][] adjustedOccupancyGrid = new int[SIZE][SIZE];
        for(int i = 0; i<SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                adjustedOccupancyGrid[i][j] = occupancyGrid[j][i];
            }
        }
        occupancyGrid = adjustedOccupancyGrid;
    }
    void writeMap(int [][] map, String outputFile){
        String text = "";
        BufferedWriter output = null;
        try {
            File file = new File(outputFile);
            output = new BufferedWriter(new FileWriter(file));

            for(int j = SIZE-1; j>=0; j--) {
                for (int i = 0; i < SIZE; i++) {
                    text += (map[i][j]==WAYPOINT?"*":Integer.toString(map[i][j]));
                }
                text += "\n";
                output.write(text);
                text = "";
            }

            output.write(text);
            output.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }
    void printMap(int [][] map){
        for(int j = SIZE-1; j>=0; j--) {
            for (int i = 0; i < SIZE; i++) {
                System.out.print(String.format("%-3s" , (map[i][j]==WAYPOINT?"*":Integer.toString(map[i][j]))));
            }
            println();
        }
        println();
    }
    int readPlanLength(String inputFile){
        Scanner scanner;
        try {
            scanner = new Scanner(new File(inputFile));
            pLength = scanner.nextInt();
        }
        catch(FileNotFoundException ex) {
            println("Unable to open map file.");
        }
        return 0;
    }
    void readPlan(String inputFile){
        Scanner scanner;
        try {
            scanner = new Scanner(new File(inputFile));
            scanner.nextInt();
            plan = new double[pLength];
            for(int i=0; i<pLength; i++){
                plan[i] = scanner.nextDouble();
            }
        }
        catch(FileNotFoundException ex) {
            println("Unable to open map file.");
        }
    }
    void printPlan(){
        System.out.print(pLength+" ");
        for(int i=0; i< pLength; i++){
            System.out.print(plan[i]+" ");
        }
    }
    void writePlan(String outputFile, String text){
        BufferedWriter output = null;
        try {
            File file = new File(outputFile);
            output = new BufferedWriter(new FileWriter(file));
            output.write(text);
            output.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}