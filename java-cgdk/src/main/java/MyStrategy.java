import model.*;
import java.util.Arrays;
public final class MyStrategy implements Strategy {
    
    public double[] findEnemy(Player me) {
        double x = 1024, y = 1024;       
        
        for (Vehicle v: updated_vehicles) {
            if (v.getPlayerId() == me.getId())
                continue;
            if (v.getDurability() == 0)
                continue;
                x = v.getX();
                y = v.getY();
                break;
        }
        return new double[]{x, y};
    }
    
    public double[] getTopLeft(Player me, VehicleType type) {
        double x = 1024, y = 1024;
        
        for (Vehicle v: vehicles) {
            if (v.getPlayerId() != me.getId())
                continue;
            if (v.getType() != type) 
                continue;
            //System.out.println(v.getType() + " " + v.getX() + " " + v.getY());
            if (v.getX() < x)
                x = v.getX();
            if (v.getY() < y)
                y = v.getY();
        }
        return new double[] {x, y};
    }
    
    @Override
    public void move(Player me, World world, Game game, Move move) {
        // System.out.println(game.getBaseActionCount()); // this is 12
        // System.out.println(game.getMaxUnitGroup()); // this is 100
        // System.out.println(getNewVehicles().length); // this is 1000 at tick_index 0
        // System.out.println(world.getHeight() + " " + world.getWidth()); // 1024
        
        int tick = world.getTickIndex();
        VehicleUpdate[] vehicle_updates = world.getVehicleUpdates();
        // System.out.println(tick);
        
        if (vehicle_updates.length == 0) {
            cnt++;
        } else {
            cnt = 0;
        }
        
        for (VehicleUpdate update: vehicle_updates) {
            for (int i = 0; i < vehicles.length; ++i) {
                if (vehicles[i].getId() == update.getId()) {
                    updated_vehicles[i] = new Vehicle(updated_vehicles[i], update);
                    break;
                }
            }
        }
        
        if (tick == 0) {
            vehicles = world.getNewVehicles();
            double [] x = new double[5];
            double [] y = new double[5];
            for (int i = 0; i < 5; ++i) {
                positions[i] = getTopLeft(me, types[i]);
                x[i] = positions[i][0];
                y[i] = positions[i][1];
            }
            
            Arrays.sort(x);
            Arrays.sort(y);
            
            Arrays.sort(positions, new java.util.Comparator<double[]>() {
                public int compare(double[] a, double[] b) {
                    return Double.compare(a[0], b[0]);
                }
            });
            
            positions[2] = new double[] {x[2], y[2]};
            
            for (int i = 0; i < 5; ++i) {
                System.out.println(Arrays.toString(positions[i]));
            }
            
            System.out.println();
            updated_vehicles = vehicles;
            return;
        }        
        
        // scale ARRV
        
        /*if (tick == 1) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.ARRV);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return;
        }
        
        if (tick == 2) {
            move.setAction(ActionType.SCALE);
            double [] start = getTopLeft(me, VehicleType.ARRV);
            move.setX(start[0]);
            move.setY(start[1]);
            move.setFactor(2);
            return;
        }*/
        
        // move figher to arrv
        
        if (tick == 50) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.FIGHTER);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return;
        }

        if (tick == 51) {
            double [] start = getTopLeft(me, VehicleType.FIGHTER);
            double [] end = positions[2];
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0] + 3);
            move.setY(end[1] - start[1] + 3);
            return;
        }
        
        // scale tank
        
        /*if (tick == 300) {
            move.setAction(ActionType.ADD_TO_SELECTION);
            move.setVehicleType(VehicleType.TANK);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return;
        }
        
        if (tick == 301) {
            move.setAction(ActionType.SCALE);
            double [] start = getTopLeft(me, VehicleType.TANK);
            move.setX(start[0]);
            move.setY(start[1]);
            move.setFactor(2);
            return;
        }*/
        
        // move tank to arrv
        
        if (tick == 52) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.TANK);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return;
        }

        if (tick == 53) {
            double [] start = getTopLeft(me, VehicleType.TANK);
            double [] end = positions[2];
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0] + 3);
            move.setY(end[1] - start[1] + 3);
            move.setMaxSpeed(game.getTankSpeed());
            return;
        }
        
        // move helicopter to ifv
        
        if (tick == 54) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.HELICOPTER);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return;
        }

        if (tick == 55) {
            double [] start = getTopLeft(me, VehicleType.HELICOPTER);
            double [] end = positions[2];
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0] + 3);
            move.setY(end[1] - start[1] + 3);
            return;
        }
        
        // scale ifv
        
        /*if (tick == 750) {
            move.setAction(ActionType.ADD_TO_SELECTION);
            move.setVehicleType(VehicleType.IFV);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return;
        }
        
        if (tick == 751) {
            move.setAction(ActionType.SCALE);
            double [] start = getTopLeft(me, VehicleType.IFV);
            move.setX(start[0]);
            move.setY(start[1]);
            move.setFactor(2);
            return;
        }*/
        
        // move ifv to arrv
        
        if (tick == 56) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.IFV);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return;
        }

        if (tick == 57) {
            double [] start = getTopLeft(me, VehicleType.IFV);
            double [] end = positions[2];
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0] + 3);
            move.setY(end[1] - start[1] + 3);
            move.setMaxSpeed(game.getIfvSpeed());
            
            return;
        }
        
        if (tick == 58) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.ARRV);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return;
        }

        if (tick == 59) {
            double [] start = getTopLeft(me, VehicleType.ARRV);
            double [] end = positions[2];
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0] + 3);
            move.setY(end[1] - start[1] + 3);
            move.setMaxSpeed(game.getIfvSpeed());
            
            return;
        }
        
        // select all
        
        if (tick == 1000) {
            move.setAction(ActionType.ADD_TO_SELECTION);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return;
        }
        
        // scale all
        
        /*if (tick == 1003) {
            move.setAction(ActionType.SCALE);
            double [] start = getTopLeft(me, VehicleType.ARRV);
            move.setX(start[0]);
            move.setY(start[1]);
            move.setFactor(0.5);
            return;
        }*/
        
        if (tick == 2500) {
            double[] start = findEnemy(world.getOpponentPlayer());
            double[] end = findEnemy(me);
            
            System.out.println();
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0]);
            move.setY(end[1] - start[1]);
            move.setMaxSpeed(game.getTankSpeed());
            return;
        }
        
        
        if (tick == 5000) {
            double[] start = findEnemy(world.getOpponentPlayer());
            double[] end = findEnemy(me);
            
            System.out.println();
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0]);
            move.setY(end[1] - start[1]);
            move.setMaxSpeed(game.getTankSpeed());
            return;
        }
        
        
        
        if (tick == 7500) {
            double[] start = findEnemy(world.getOpponentPlayer());
            double[] end = findEnemy(me);
            
            System.out.println();
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0]);
            move.setY(end[1] - start[1]);
            move.setMaxSpeed(game.getTankSpeed());
            return;
        }
        
        
        
        if (tick == 1000) {
            double[] start = findEnemy(world.getOpponentPlayer());
            double[] end = findEnemy(me);
            
            System.out.println();
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0]);
            move.setY(end[1] - start[1]);
            move.setMaxSpeed(game.getTankSpeed());
            return;
        }
    }
    private Vehicle[] vehicles;    
    private Vehicle[] updated_vehicles;
    private VehicleType[] types = {VehicleType.TANK, 
                                   VehicleType.IFV, 
                                   VehicleType.HELICOPTER,     
                                   VehicleType.FIGHTER,
                                   VehicleType.ARRV};
    private double[][] positions = new double[5][2];
    private int cnt = 0;
    private boolean move = false;
}
