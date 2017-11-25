import model.*;
import java.util.Arrays;
public final class MyStrategy implements Strategy {
    
    @Override
    public void move(Player me, World world, Game game, Move move) {
        // System.out.println(game.getBaseActionCount()); // this is 12
        // System.out.println(game.getMaxUnitGroup()); // this is 100
        // System.out.println(getNewVehicles().length); // this is 1000 at tick_index 0
        // System.out.println(world.getHeight() + " " + world.getWidth()); // 1024
        
        this.me = me;
        this.world = world;
        this.game = game;
        this.move = move;
        
        int tick = world.getTickIndex();
        
        if (tick == 0)
            init_vehicles();
        
        update_vehicle();
        
        if (tick < 1000) {
            line_up(tick);
            return;
        }
        
        // scale all
        
        /*if (tick == 1003) {
            move.setAction(ActionType.SCALE);
            double [] start = find_vehicle(me, VehicleType.ARRV);
            move.setX(start[0]);
            move.setY(start[1]);
            move.setFactor(0.5);
            return;
        }*/
        
        if (tick < 1000 && tick % 500 == 0) {
            double[] start = find_nearest_enemy(world.getOpponentPlayer(), true);
            move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);
            move.setX(start[0]);
            move.setY(start[1]);
        }
        
        if (tick >= 1000 && tick % 500 == 0) {
            double[] start = find_nearest_enemy(world.getOpponentPlayer(), false);
            double[] end = find_nearest_enemy(me, false);
            
            System.out.println();
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0]);
            move.setY(end[1] - start[1]);
            move.setMaxSpeed(game.getTankSpeed() * game.getSwampTerrainSpeedFactor());
            return;
        }
    }
    
    private Vehicle[] origin_vehicles;    
    private Vehicle[] vehicles;
    
    private World world;
    private Player me;
    private Game game;
    private Move move;
    
    private VehicleType[] types = {VehicleType.TANK, 
                                   VehicleType.IFV, 
                                   VehicleType.HELICOPTER,     
                                   VehicleType.FIGHTER,
                                   VehicleType.ARRV};
    
    private void update_vehicle() {
        VehicleUpdate[] vehicle_updates = world.getVehicleUpdates();
        // System.out.println(tick);
        
        for (VehicleUpdate vehicle_update: vehicle_updates) {
            for (int i = 0; i < origin_vehicles.length; ++i) {
                if (origin_vehicles[i].getId() == vehicle_update.getId()) {
                    vehicles[i] = new Vehicle(vehicles[i], vehicle_update);
                    break;
                }
            }
        }
    }
    
    private double[] find_nearest_enemy(Player me, boolean is_tank) {
        double x = 1024, y = 1024;       
        
        for (Vehicle v: vehicles) {
            if (v.getPlayerId() == me.getId())
                continue;
            if (v.getDurability() == 0)
                continue;
            
            x = v.getX();
            y = v.getY();
            if (!is_tank)
                break;
            if (v.getType() == VehicleType.TANK)
                break;
        }
        return new double[]{x, y};
    }
    
    private double[] find_vehicle(Player me, VehicleType type) {
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
    
    private void init_vehicles() {
        origin_vehicles = world.getNewVehicles();
        vehicles = origin_vehicles;
        
        double [] x = new double[5];
        double [] y = new double[5];
        for (int i = 0; i < 5; ++i) {
            positions[i] = find_vehicle(me, types[i]);
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
    }
    
    private double[][] positions = new double[5][2];      

    private void mix(VehicleType v1, VehicleType v2) {
        double [] pv1 = find_vehicle(me, v1);
        double [] pv2 = find_vehicle(me, v2);
        
        
    }
    
    private void scale(VehicleType v, int tick) {
        double [] pv = find_vehicle(me, v);        
        
        int mod = 2;
        
        if (tick % mod == 0) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.FIGHTER);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight()); 
            
        }
        
    }
    
    private void line_up(int tick) {        
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
            double [] start = find_vehicle(me, VehicleType.ARRV);
            move.setX(start[0]);
            move.setY(start[1]);
            move.setFactor(2);
            return;
        }*/
        
        // move figher to middle
        
        if (tick == 50) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.FIGHTER);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return ;
        }

        if (tick == 51) {
            double [] start = find_vehicle(me, VehicleType.FIGHTER);
            double [] end = positions[2];
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0] + 3);
            move.setY(end[1] - start[1] + 3);
            return ;
        }
        
        if (tick == 52) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.TANK);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return ;
        }

        if (tick == 53) {
            double [] start = find_vehicle(me, VehicleType.TANK);
            double [] end = positions[2];
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0] + 3);
            move.setY(end[1] - start[1] + 3);
            move.setMaxSpeed(game.getTankSpeed());
            return ;
        }
        
        if (tick == 54) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.HELICOPTER);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return ;
        }

        if (tick == 55) {
            double [] start = find_vehicle(me, VehicleType.HELICOPTER);
            double [] end = positions[2];
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0] + 3);
            move.setY(end[1] - start[1] + 3);
            return ;
        }
        
        if (tick == 56) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.IFV);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return ;
        }

        if (tick == 57) {
            double [] start = find_vehicle(me, VehicleType.IFV);
            double [] end = positions[2];
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0] + 3);
            move.setY(end[1] - start[1] + 3);
            move.setMaxSpeed(game.getIfvSpeed());
            
            return ;
        }
        
        if (tick == 58) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(VehicleType.ARRV);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return ;
        }

        if (tick == 59) {
            double [] start = find_vehicle(me, VehicleType.ARRV);
            double [] end = positions[2];
            System.out.println(Arrays.toString(start));
            System.out.println(Arrays.toString(end));
            move.setAction(ActionType.MOVE);
            move.setX(end[0] - start[0] + 3);
            move.setY(end[1] - start[1] + 3);
            move.setMaxSpeed(game.getIfvSpeed());
            
            return ;
        }
        
        // select all
        
        if (tick == 60) {
            move.setAction(ActionType.ADD_TO_SELECTION);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return ;
        };
    }
}
