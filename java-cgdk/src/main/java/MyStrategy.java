import model.*;
import java.util.*;
public final class MyStrategy implements Strategy {
    private void print_info(World world, Game game) {
        // System.out.println(world.getNewVehicles().length); // this is 1000 at tick_index 0
        // System.out.println(world.getHeight() + " " + world.getWidth()); // 1024
        // System.out.println(world.getTickCount()); // 20000
        
        // System.out.println(game.getMaxUnitGroup()); // this is 100
        
        // System.out.println(game.getTacticalNuclearStrikeRadius()); // 50
        // System.out.println(game.getBaseTacticalNuclearStrikeCooldown()); // 1200
        
        // System.out.println(game.getActionDetectionInterval()); // 60
        // System.out.println(game.getBaseActionCount()); // this is 12
        // System.out.println(game.getAdditionalActionCountPerControlCenter()); // 3
        
        // System.out.println(game.getMaxFacilityCapturePoints()); // 100
        
        System.out.println("vehicle radius: " + game.getVehicleRadius());
        
        System.out.println("tank speed: " + game.getTankSpeed());
        System.out.println("swamp factor: " + game.getSwampTerrainSpeedFactor()); // 333
        
        System.out.println();
        
        delta = game.getVehicleRadius();
        factor = 3;
        
        assigned = new HashMap();
        
        facilities = world.getFacilities();
        Arrays.sort(facilities, new Comparator<Facility>() {
            public int compare(Facility o1, Facility o2) {
                double d1 = o1.getLeft() * o1.getLeft() + o1.getTop() * o1.getTop();
                double d2 = o2.getLeft() * o2.getLeft() + o2.getTop() * o2.getTop();
                
                return Double.valueOf(d1).compareTo(Double.valueOf(d2));
            }
        });
        
        System.out.println("facilities: " + facilities.length);
        for (Facility f: facilities) {
            System.out.println(f.getType() + " " + f.getLeft( ) + " " + f.getTop());
        }
        System.out.println();
    }
    
    @Override    
    public void move(Player me, World world, Game game, Move move) {        
        this.me = me;
        this.world = world;
        this.game = game;
        this.move = move;
        
        int tick = world.getTickIndex();
                
        // vehicle
        update_vehicle();
        Vehicle[] new_vehicles = world.getNewVehicles();
        
        if (tick == 0) {
            print_info(world, game);
        }
        
        if (tick == 0)
            init_vehicles();
        
        if (me.getRemainingActionCooldownTicks() != 0)
            return;
        
        if (tick % game.getBaseTacticalNuclearStrikeCooldown() == 0) {
            boolean code = nuclear_strike();
            if (code)
                
                return;
        }
        
        if (tick < 2000) {
            line_up(tick);
            return;
        }
        
        if (tick < 10) {
            assign(vehicleTypes[tick / 2], tick / 2 + 1, tick % 2);
            return;
        }
        
        // facility
        for (Facility f: facilities) {
            if (f.getCapturePoints( ) != game.getMaxFacilityCapturePoints()) {
                System.out.println(f.getCapturePoints( ) + " " + game.getMaxFacilityCapturePoints());
                double [] xy = get_center(0, me);
                move_to(0, f.getLeft() - xy[0], f.getTop() - xy[1], tick);
                return;
            }
            if (f.getType() == FacilityType.VEHICLE_FACTORY) {
                if (f.getOwnerPlayerId() == me.getId()) {
                    if (f.getVehicleType() == null) {
                        move.setAction(ActionType.SETUP_VEHICLE_PRODUCTION);
                        move.setFacilityId(f.getId());
                        move.setVehicleType(VehicleType.TANK);
                        return;
                    } else {
                        // System.out.println("production progress: " + " " + f.getProductionProgress());
                    }
                }
            }
            else if (f.getType() == FacilityType.CONTROL_CENTER) {
                
            }
        }
    }
    
    private void assign(VehicleType type, int group, int tick) {
        if (tick == 0) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setVehicleType(type);
            move.setRight(world.getWidth());
            move.setBottom(world.getHeight());
        } else {
            move.setAction(ActionType.ASSIGN);
            move.setGroup(group);
        }
    }
    
    private boolean nuclear_strike() {
        ArrayList<Vehicle> vehicle_me = new ArrayList<Vehicle>();
        ArrayList<Vehicle> vehicle_enemy = new ArrayList<Vehicle>();
        
        for (Vehicle v: vehicles) {
            if (v.getDurability() == 0)
                continue;
            
            if (v.getPlayerId() != me.getId()) {
                vehicle_enemy.add(v);
            } else {
                vehicle_me.add(v);
            }
        }
        
        double x = world.getWidth();
        double y = world.getHeight();
        long vehicleId = -1;
        
        Iterator me_it = vehicle_me.iterator();
        while (me_it.hasNext()) {
            if (vehicleId != -1)
                break;
            Vehicle v = (Vehicle)me_it.next();
            Iterator enemy_it = vehicle_enemy.iterator();
            while (enemy_it.hasNext()) {
                Vehicle v_ = (Vehicle)enemy_it.next();
                double distance = v.getDistanceTo(v_);
                double vision = v.getVisionRange();
                double nr = game.getTacticalNuclearStrikeRadius();
                if (distance < vision) {
                    x = v_.getX() + (v_.getX() - v.getX()) / distance * nr;
                    y = v_.getY() + (v_.getY() - v.getY()) / distance * nr;
                    vehicleId = v.getId();
                    break;
                }
            }
        }
        
        if (vehicleId != -1) {
            nuclear_strike(x, y, vehicleId);
            return true;
        }
        return false;
    }
    
    private void nuclear_strike(double x, double y, long vehicle_id) {
        move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);            
        move.setVehicleId(vehicle_id);        
        move.setX(x);
        move.setY(y);            
            
        System.out.println("nuclear strick: " + x + " " + y);
        System.out.println();
    }
    
    private double [] get_center(int group, Player me) {
        double x = 0, y = 0;
        int cnt = 0;
        
        for (Vehicle v: vehicles) {
            if (v.getPlayerId() != me.getId())
                continue;
            if (v.getDurability() == 0)
                continue;
            if (v.isAerial())
                continue;
            if (group <= 0){
                x += v.getX();
                y += v.getY();
                cnt += 1;                
                continue;
            }
            int [] groups = v.getGroups();
            for (int g: groups) {                 
                if (g == group) {
                    x += v.getX();
                    y += v.getY();
                    cnt += 1;
                    break;
                }
            }
        }
        return new double[] {x / cnt, y / cnt};
    }
    
    private void move_to(int group, double x, double y, int tick) {
        System.out.println("Group " + group + " move_to " + x + " " + y + " at tick " + tick);
        tick %= 2;
        
        if (tick == 0) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            if (group > 0)
                move.setGroup(group);
            else {
                move.setRight(world.getWidth());
                move.setBottom(world.getHeight());
            }
        }
        
        if (tick == 1) {
            int cnt = 0;
            for (Vehicle v: vehicles) {
                if (v.isSelected())
                    cnt++;
            }
            System.out.println("cnt selected: " + cnt);
            move.setAction(ActionType.MOVE);
            move.setX(x);
            move.setY(y);
            move.setMaxSpeed(game.getTankSpeed() * game.getSwampTerrainSpeedFactor());
        }
    }
    
    private void scale(int group, double x, double y, double factor, int tick) {
        
        if (tick == 0) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setGroup(group);
        }
        
        if (tick == 1) {            
            move.setAction(ActionType.SCALE);
            move.setX(x);
            move.setY(y);
            move.setFactor(factor);
            move.setMaxSpeed(game.getTankSpeed());
        }
    }
    
    private void rotate(int group, double x, double y, double angle, int tick) {
        if (tick == 0) {
            move.setAction(ActionType.CLEAR_AND_SELECT);
            move.setGroup(group);
        }
        
        if (tick == 1) {
            move.setAction(ActionType.ROTATE);
            move.setX(x);
            move.setY(y);
            move.setAngle(angle); // 3.1415 / 4
            move.setMaxSpeed(game.getTankSpeed() * game.getSwampTerrainSpeedFactor());
        }
        return;
    }
    
    private void init_vehicles() {
        vehicles = world.getNewVehicles();
        System.out.println("vehicle length: " + vehicles.length);
        for (Vehicle v: vehicles) {
            // System.out.println(v.getX() + " " + v.getY() + " " +  v.getType());
        }
        System.out.println();
        
        Vehicle v_;
        for (int i = 0; i < 3; ++i) {
            v_ = find_vehicle(me, vehicleTypes[i]);
            positions0[i] = new Position(v_.getX(), v_.getY(), v_.getType());
        }
        
        for (int i = 3; i < 5; ++i) {
            v_ = find_vehicle(me, vehicleTypes[i]);
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
    
    private void update_vehicle() {
        VehicleUpdate[] vehicle_updates = world.getVehicleUpdates();
        
        for (VehicleUpdate vehicle_update: vehicle_updates) {
            for (int i = 0; i < vehicles.length; ++i) {
                if (vehicles[i].getId() == vehicle_update.getId()) {
                    vehicles[i] = new Vehicle(vehicles[i], vehicle_update);
                    break;
                }
            }
        }
    }
    
    private Map assigned; 
    
    private Vehicle[] vehicles;
    private Facility[] facilities;
    
    private VehicleType[] vehicleTypes = {VehicleType.TANK, 
                                          VehicleType.IFV,
                                          VehicleType.ARRV,
                                          VehicleType.HELICOPTER,
                                          VehicleType.FIGHTER};
    
    private double delta;
    private double factor;
    
    private World world;
    private Player me;
    private Game game;
    private Move move;
    
    
    
    
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
    
    
    private Position [] positions0 = new Position[3];
    private Position [] positions1 = new Position[2];
    
    private double left = 18, middle = 92 - 7 * delta, right = 166 - 14 * delta;
}