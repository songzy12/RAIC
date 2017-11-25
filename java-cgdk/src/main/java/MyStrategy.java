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
        
        if (tick < 3000) {
            line_up(tick);
            return;
        }
        
        if (tick % 1200 == 0) {
            Vehicle v = find_nearest_enemy(world.getOpponentPlayer(), true);
            move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);
            move.setX(v.getX());
            move.setY(v.getY());
            move.setVehicleId(find_vehicle(me, VehicleType.ARRV).getId());
        }
        
        if (tick > 3000 && tick % 500 == 0) {
            Vehicle v1 = find_nearest_enemy(world.getOpponentPlayer(), false);
            Vehicle v2 = find_nearest_enemy(me, false);
            double[] start = {v1.getX(), v1.getY()};
            double[] end = {v2.getX(), v2.getY()};
            
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
    
    private Vehicle find_nearest_enemy(Player me, boolean is_tank) {
        Vehicle v_ = null;
        double x = 1024, y = 1024;       
        
        for (Vehicle v: vehicles) {
            if (v.getPlayerId() == me.getId())
                continue;
            if (v.getDurability() == 0)
                continue;
            
            v_ = v;
            if (!is_tank)
                break;
            if (v.getType() == VehicleType.TANK)
                break;
        }
        return v_;
    }
    
    private Vehicle find_vehicle(Player me, VehicleType type) {
        Vehicle v_ = null;
        double x = 1024, y = 1024;
        
        for (Vehicle v: vehicles) {
            if (v.getPlayerId() != me.getId())
                continue;
            if (v.getType() != type) 
                continue;
            //System.out.println(v.getType() + " " + v.getX() + " " + v.getY());
            if (v.getX() < x)
                v_ = v;
            if (v.getY() < y)
                v_ = v;
        }
        return v_;
    }
    
    private double[][] positions = new double[5][2];      
    
    private void move_to(VehicleType v, double x, double y, int tick) {
        
        
        if (tick == 0) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(v);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
        }
        
        if (tick == 1) {
            System.out.println("move_to, " + x + " " + y);
        
            move.setAction(ActionType.MOVE);
            move.setX(x);
            move.setY(y);
            move.setMaxSpeed(game.getTankSpeed() * game.getSwampTerrainSpeedFactor());
        }
    }
    
    private void scale(VehicleType v, int tick, double factor) {
        Vehicle v_ = find_vehicle(me, v);
        double [] pv = {v_.getX(), v_.getY()};
        
        if (tick == 0) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(v);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight()); 
        }
        
        if (tick == 1) {            
            move.setAction(ActionType.SCALE);
            v_ = find_vehicle(me, v);
            double delta = game.getVehicleRadius();
            double [] origin = {v_.getX(), v_.getY()};
            move.setX(origin[0]);
            move.setY(origin[1]);
            move.setFactor(factor);
            move.setMaxSpeed(game.getTankSpeed());
        }
    }
    
    private void line_up(int tick) {
        
        // move further
        if (tick < 10) {
            for (int i = 0; i < 5; ++i) {
                double delta = 30 * game.getVehicleRadius() + 2 * i * game.getVehicleRadius();
                
                move_to(types[i], delta, delta, tick - i * 2);
            }
        }
        
        // scale 
        if (tick >= 500 && tick < 510) {
            double factor = 3;
            for (int i = 0; i < 5; ++i)
                scale(types[i], tick - 500 - i * 2, factor);
            return;
        }
        
        // intersect
        if (tick >= 1000 && tick < 1010) {
            double mid_x = positions[2][0] + 30 * game.getVehicleRadius();        
            for (int i = 0; i < 5; ++i) {
                Vehicle v_ =  find_vehicle(me, types[i]);
                double delta = game.getVehicleRadius();
                if (Math.abs(v_.getX() - mid_x) > delta)
                    move_to(types[i], mid_x + 2 * i * delta - v_.getX() , 0, tick - 1000 - i * 2);
            }
            return;
        }
        
        
        if (tick >= 1500 && tick < 1510) {
            double mid_y = positions[2][1] + 30 * game.getVehicleRadius();            
            for (int i = 0; i < 5; ++i) {
                Vehicle v_ =  find_vehicle(me, types[i]);
                double delta = game.getVehicleRadius();
                if (Math.abs(v_.getY() - mid_y) > delta)
                    move_to(types[i], 0, mid_y + 2 * i * delta - v_.getY() , tick - 1500 - i * 2);
            }
            return;
        }
        
        if (tick >= 2000 && tick < 2002) {
            double mid_x = positions[2][0] + 30 * game.getVehicleRadius();                    
            double mid_y = positions[2][1] + 30 * game.getVehicleRadius();
                
            tick -= 2000;
            double factor = 1.0 / 3;
            if (tick == 0) {
                move.setAction(ActionType.CLEAR_AND_SELECT);
                move.setRight(world.getWidth());
                move.setBottom(world.getHeight()); 
            }
            
            if (tick == 1) {            
                move.setAction(ActionType.SCALE);
                move.setX(mid_x);
                move.setY(mid_y);
                move.setFactor(factor);
                move.setMaxSpeed(game.getTankSpeed());
            }
            return;
        }
            
        
        // select all
        if (tick == 2500) {
            move.setAction(ActionType.ADD_TO_SELECTION);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
            return ;
        };
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
    
    private void init_vehicles() {
        origin_vehicles = world.getNewVehicles();
        vehicles = origin_vehicles;
        
        double [] x = new double[5];
        double [] y = new double[5];
        Vehicle v_;
        for (int i = 0; i < 5; ++i) {
            v_ = find_vehicle(me, types[i]);
            positions[i] = new double[] {v_.getX(), v_.getY()};
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
}
