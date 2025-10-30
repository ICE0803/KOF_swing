package Client.Character;

import java.awt.*;

import java.util.ArrayList;


public class Character {
    public static final boolean LEFT = true;
    public static final boolean RIGHT = false;

    private String name;//角色名字

    private int HP = 5;//血量

    private  ArrayList<Image> movements;//人物角色的动作：左右停

    private int SPEED = 15;//当前移动速度（放大人物后适当提高速度）

    private Point position;//当前位置

    private Dir dir;//动作按钮类
    
    private long lastAttackTime = 0;//上次攻击时间
    private static final long ATTACK_COOLDOWN = 500;//攻击冷却时间（毫秒）


    public Character(String name,boolean leftOrRight,String rootDir,int x,int y) {
        Toolkit tk =Toolkit.getDefaultToolkit();
        this.name = name;

        movements = new ArrayList<Image>();
        position = new Point(x,y);


        for(int i = 1; i <= 8; i++) {//将图片加载到ArrayList
            movements.add(tk.getImage(Character.class.getClassLoader()
                    .getResource(rootDir + "/" + i + ".gif")));
        }

        dir = new Dir(leftOrRight);//监控动作
        dir.setCurrentDir(leftOrRight);
        dir.createMap(movements);//创建对应动作maps

    }//将图片载入缓存区, 并且做好索引


    //将当前动作画上面板（可以重写调整每一帧动作的图片位置）
    protected void drawCurrentMovement(Graphics g) {
        Image currentMovement = dir.getCurrentMovement();
        dir.locateDirection();//更新目前的动作
        g.drawImage(currentMovement,
                (int)position.getX(),
                (int)position.getY(),
                currentMovement.getWidth(null) * 2,
                currentMovement.getHeight(null) * 2,null);

    }

    /**
     * 当fighter1成功攻击fighter2时，返回true，否则返回false
     * @param fighter1
     * @param fighter2
     * @return
     */
    public static boolean isAttacked(Character fighter1,Character fighter2) {
        Point p1 = fighter1.getPosition();
        Point p2 = fighter2.getPosition();

        //攻击有效范围 - 修复攻击检测逻辑
        if(!fighter2.getDir().FALL) {
            // 检查水平方向重叠 - 修复逻辑错误
            int horizontalDistance = Math.abs(p1.x - p2.x);
            boolean horizontalOverlap = horizontalDistance <= 100; // 原来是60，现在放大3倍为180
            
            // 检查垂直方向重叠
            int verticalDistance = Math.abs(p1.y - p2.y);
            boolean verticalOverlap = verticalDistance <= 30; // 原来是30，现在放大3倍为90
            
            if (horizontalOverlap && verticalOverlap) {
                System.out.println("attack! 攻击者位置: (" + p1.x + ", " + p1.y + "), 被攻击者位置: (" + p2.x + ", " + p2.y + ")");
                System.out.println("水平距离: " + horizontalDistance + ", 垂直距离: " + verticalDistance + " - 攻击成功！");
                return true;
            } else {
                System.out.println("攻击检测: 攻击者位置: (" + p1.x + ", " + p1.y + "), 被攻击者位置: (" + p2.x + ", " + p2.y + ")");
                System.out.println("水平距离: " + horizontalDistance + " (需要<=180), 垂直距离: " + verticalDistance + " (需要<=90) - 攻击失败");
            }
        } else {
            System.out.println("攻击检测: 被攻击者处于FALL状态，无法被攻击");
        }
        return false;
    }

    public Dir getDir() {
        return dir;
    }
    public Point getPosition() {
        return position;
    }
    public int getSPEED() {
        return SPEED;
    }
    public int getHP() {
        return HP;
    }
    public void setHP(int HP) {
        this.HP = HP;
    }
    
    /**
     * 设置移动速度
     * @param speed 新的移动速度
     */
    public void setSPEED(int speed) {
        this.SPEED = speed;
    }
    
    /**
     * 检查是否可以攻击（冷却时间是否结束）
     * @return 是否可以攻击
     */
    public boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime >= ATTACK_COOLDOWN;
    }
    
    /**
     * 设置攻击时间，开始冷却
     */
    public void setAttackTime() {
        lastAttackTime = System.currentTimeMillis();
    }
    
    /**
     * 获取剩余冷却时间
     * @return 剩余冷却时间（毫秒）
     */
    public long getRemainingCooldown() {
        long elapsed = System.currentTimeMillis() - lastAttackTime;
        return Math.max(0, ATTACK_COOLDOWN - elapsed);
    }
}

