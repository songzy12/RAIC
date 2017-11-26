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
        
        if (tick < 2000) {
            line_up(tick);
            return;
        }
        
        if (tick >= 2000 && tick < 2002) {
            tick -= 2000;
            if (tick == 0) {
                move.setAction(ActionType.CLEAR_AND_SELECT);
                move.setRight(world.getWidth());
                move.setBottom(world.getHeight());       
            }
            
            if (tick == 1) {
                move.setAction(ActionType.ROTATE);
                move.setX((middle + right) / 2);
                move.setY((middle + right) / 2);
                move.setAngle(3.1415 / 4);
                move.setMaxSpeed(game.getTankSpeed() * game.getSwampTerrainSpeedFactor());
            }
            return;
        }
        
        if (tick >= 2500 && (tick % 60 == 25 || tick % 60 == 30)) {
            if (tick % 60 == 25) {
                re_line_up(tick);
            }
            if (tick % 60 == 30) {
                double[] start = get_center(me);
                Vehicle v2 = find_vehicle(world.getOpponentPlayer());
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
    }
    
    private void re_line_up(int tick) {
        double [] position = get_center(me);
        
        move.setAction(ActionType.SCALE);
        move.setX(position[0]);
        move.setY(position[1]);
        move.setFactor(0.1);
        move.setMaxSpeed(game.getTankSpeed());
    }
    
    private double [] get_center(Player me) {
        double x = 0, y = 0;
        int cnt = 0;
        
        for (Vehicle v: vehicles) {
            if (v.getPlayerId() != me.getId())
                continue;
            if (v.getDurability() == 0)
                continue;
            if (v.getType() == VehicleType.HELICOPTER)
                continue;
            if (v.getType() == VehicleType.FIGHTER)
                continue;
            x += v.getX();
            y += v.getY();
            cnt += 1;
        }
        return new double[] {x / cnt, y / cnt};
    }
    
    private Vehicle find_vehicle(Player me) {
        if (this.me.getId() != me.getId()) {
            Vehicle v_ = null;
            double [] center = get_center(this.me);
            double d = 1024 * 1024;
            
            for (Vehicle v: vehicles) {
                if (v.getPlayerId() != me.getId())
                    continue;
                if (v.getDurability() == 0)
                    continue;
                double d_ = v.getDistanceTo(center[0], center[1]);
                if (d_ < d) {
                    d = d_;
                    v_ = v;   
                }
            }
            return v_;
        } else {
            Vehicle v_ = null;
            double x = 1024, y = 1024;
            
            for (Vehicle v: vehicles) {
                if (v.getPlayerId() != me.getId())
                    continue;
                if (v.getDurability() == 0)
                    continue;
                
                if (v.getX() <= x || v.getY() <= y) {
                    x = v.getX();
                    y = v.getY();
                    v_ = v;   
                }
            }
            return v_;
        }
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
            if (v.getX() <= x && v.getY() <= y) {
                x = v.getX();
                y = v.getY();
                v_ = v;   
            }
        }
        return v_;
    }
    
    private void move_to(VehicleType v, double x, double y, int tick) {
        if (tick == 0) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(v);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());       
        }
        
        if (tick == 1) {
            System.out.println(v + " move_to " + x + " " + y);
            move.setAction(ActionType.MOVE);
            move.setX(x);
            move.setY(y);
        }
    }
    
    private void scale(VehicleType v, double x, double y, double factor, int tick) {
        
        if (tick == 0) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(v);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight()); 
        }
        
        if (tick == 1) {            
            move.setAction(ActionType.SCALE);
            move.setX(x);
            move.setY(y);
            move.setFactor(factor);
            move.setMaxSpeed(game.getTankSpeed());
        }
    }
    
    private void line_up(int tick) {
        
        if (tick < 700) {        
            // permutate
            if (tick < 2 && positions0[0].x != left) {
                move_to(positions0[0].type, left - positions0[0].x, 0, tick);
            } else if (tick >= 2 && tick < 4 && positions0[1].x != middle) {
                move_to(positions0[1].type, middle - positions0[1].x, 0, tick - 2);
            } else if (tick >= 4 && tick < 6 && positions0[2].x != right) {
                move_to(positions0[2].type, right - positions0[2].x, 0, tick - 4);
            } 
            
            if (tick >= 350 && tick < 352 && positions0[0].y != middle + 2 * delta) {
                move_to(positions0[0].type, 0, middle + 2 * delta - positions0[0].y, tick - 350);
            } else if (tick >= 352 && tick < 354 && positions0[1].y != middle) {
                move_to(positions0[1].type, 0, middle - positions0[1].y, tick - 352);
            } else if (tick >= 354 && tick < 356 && positions0[2].y != middle - 2 * delta) {
                move_to(positions0[2].type, 0, middle - 2 * delta - positions0[2].y, tick - 354);
            }
            
            
            if (tick >= 6 && tick < 8 && positions1[0].x != left) {
                move_to(positions1[0].type, left - positions1[0].x, 0, tick - 6);
            } else if (tick >= 8 && tick < 10 && positions1[1].x != middle) {
                // here can not be right, since it may originally be in left
                move_to(positions1[1].type, middle - positions1[1].x, 0, tick - 8);
            } 
            
            if (tick >= 356 && tick < 358 && positions1[0].y != middle) {
                move_to(positions1[0].type, 0, middle - positions1[0].y, tick - 356);
            } else if (tick >= 358 && tick < 360 && positions1[1].y != middle + 2*delta) {
                move_to(positions1[1].type, 0, middle + 2 * delta - positions1[1].y, tick - 358);
            }
            if (tick == 360)
                System.out.println();
            return;
        }
        
        if (tick >= 700 && tick < 1060) {
            int step = (tick - 700) / 60;
            int start = step * 6 + 1;
            int group = ((tick - 700) % 60) / 2 + 1;
            if (group < start)
                return;
                    
            double l = left + (group - 1) * 3 * delta - delta;
            double r = left + (group) * 3 * delta - delta;
            
            if (tick % 2 == 0) {
                // select
                System.out.println("group " + group + " " + l + " " + r);
                move.setAction(ActionType.CLEAR_AND_SELECT);
                move.setLeft(l);
                move.setRight(r);          
                move.setTop(0);
                move.setBottom(world.getHeight());                
            } else {
                // scale                
                move.setAction(ActionType.SCALE);
                move.setFactor(factor);
                move.setX(l + delta);
                move.setY(middle + (right-middle) / 2);
            }
            return;
        }
        
        
        if (tick >= 1200 && tick < 1206) {
            // merge
            if (tick >= 1200 && tick < 1202) {
                move_to(positions0[0].type, middle - left, 0, tick - 1200);
            } else if (tick >= 1202 && tick < 1204) {
                move_to(positions0[2].type, middle - right, 0, tick - 1202);
            } else if (tick >= 1204 && tick < 1206) {
                move_to(positions1[0].type, middle - left, 0, tick - 1204);
            }  
            if (tick == 1205)
                System.out.println();
            return;
        }
        
        if (tick >= 1500 && tick < 1860) {
            // scale
            int step = (tick - 1500) / 60;
            int start = step * 6 + 1;
            int group = ((tick - 1500) % 60) / 2 + 1;
            if (group < start)
                return;
                    
            double l = left + (group - 1) * 3 * delta - delta;
            double r = left + (group) * 3 * delta - delta;
            
            if (tick % 2 == 0) {
                // select
                System.out.println("group " + group + " " + l + " " + r);
                move.setAction(ActionType.CLEAR_AND_SELECT);
                move.setLeft(l);
                move.setRight(r);          
                move.setTop(0);
                move.setBottom(world.getHeight());                
            } else {
                // scale                
                move.setAction(ActionType.SCALE);
                move.setFactor(1 / factor);
                move.setX(l + delta);
                move.setY(middle + (right-middle) / 2);
            }
            return;
        }
        
        if (tick == 1860) {
            // select all
            move.setAction(ActionType.ADD_TO_SELECTION);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());    
            System.out.println();   
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
                                   VehicleType.ARRV,
                                   VehicleType.HELICOPTER,     
                                   VehicleType.FIGHTER};
    
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
        Vehicle v_;
        for (int i = 0; i < 3; ++i) {
            v_ = find_vehicle(me, types[i]);
            positions0[i] = new Position(v_.getX(), v_.getY(), v_.getType());
        }
        
        for (int i = 3; i < 5; ++i) {
            v_ = find_vehicle(me, types[i]);
            positions1[i-3] = new Position(v_.getX(), v_.getY(), v_.getType());
        }
        
        Arrays.sort(positions0, new java.util.Comparator<Position>() {
            public int compare(Position a, Position b) {
                if (a.x != b.x)
                    return Double.compare(a.x, b.x);
                return Double.compare(a.y, b.y);
            }
        });
        
        Arrays.sort(positions1, new java.util.Comparator<Position>() {
            public int compare(Position a, Position b) {
                if (a.x != b.x)
                    return Double.compare(a.x, b.x);
                return Double.compare(a.y, b.y);
            }
        });
        
        System.out.println(Arrays.toString(positions0));
        System.out.println(Arrays.toString(positions1));
        
        System.out.println("radius: " + game.getVehicleRadius());
        
        // System.out.println("distance / speed: " + (right - middle) / (game.getTankSpeed() * game.getSwampTerrainSpeedFactor())); // 333
        
        System.out.println();
    }
    
    private Position [] positions0 = new Position[3];
    private Position [] positions1 = new Position[2];
    
    private double delta = 2; //game.getVehicleRadius();
    private double left = 18, middle = 92 - 7 * delta, right = 166 - 14 * delta;
    private double factor = 3;
    
}

class Position {
    public Position(double x, double y, VehicleType type) {
            this.x = x;
            this.y = y;
            this.type = type;
    }
    
    double x;
    double y;
    VehicleType type;
}
