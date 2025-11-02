package Client.Net;

import Client.Character.Character;
import Client.Character.Dir;
import Client.Protocol.Message;
import Client.SurfaceGUI.MyFrame;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 这是一个监听server发到client的Runnable类，以将对方client的角色在本client上操控
 */
public class NetThread implements Runnable {
    private MyFrame myFrame;
    private Character fighter;
    BufferedReader in;
    private Point receivedPosition;//接收到的信息中对方的位置

    public NetThread(MyFrame myFrame,Character fighter, BufferedReader in) {
        this.myFrame = myFrame;
        this.in = in;
        this.fighter = fighter;
        receivedPosition = new Point();
    }

    public Point getReceivedPosition() {
        return receivedPosition;
    }

    @Override
    public void run() {
        String fromServer = null;

        try {
            while ((fromServer = in.readLine()) != null) {
                    System.out.println("client received: " + fromServer);

                    // 如果收到的是服务器发来的退出信息
                    if(fromServer.equals("@EXIT@")) {
                        System.out.println("Received exit command from server");
                        handleConnectionLost();
                        break;
                    }

                    // 处理服务器关闭消息
                    if(fromServer.equals("@SERVER_SHUTDOWN@")) {
                        System.out.println("Server is shutting down");
                        // 在UI线程中显示服务器关闭的消息
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null,
                                "服务器已关闭，游戏结束。",
                                "连接断开",
                                JOptionPane.INFORMATION_MESSAGE);
                        });
                        handleConnectionLost();
                        break;
                    }

                    //若对方未能成功触发攻击
                    if(!fromServer.equals("@ATTACK@")) {
                        Message m = new Message(fromServer);//解析

                        //test
                        System.out.println(m.getKeyState()
                                +" "+ m.getKeyCode() +" "+ m.getPosition().x +" "+ m.getPosition().y);

                        receivedPosition = m.getPosition();//返回一个位置


                        //判断对方按键是按下还是松开
                        if(m.getKeyState() == Message.PRESS) {
                            System.out.println("@PRESSED");
                            //fighter.getDir().getKeyPressed(m.getKeyCode());
                            myFrame.getKeyPressed(fighter,m.getKeyCode());

                            //修正不同客户端角色的位置
                            Dir.refineMovement(myFrame.getServerFighter(),getReceivedPosition());
                        }
                        else {
                            System.out.println("@RELEASED");

                            myFrame.getKeyReleased(fighter,m.getKeyCode());

                            Dir.refineMovement(myFrame.getServerFighter(),getReceivedPosition());
                        }
                    } else if(fromServer.equals("@ATTACK@")){
                        myFrame.getMyFighter().setHP(myFrame.getMyFighter().getHP() - 1);
                        myFrame.getMyFighter().getDir().fighterFall();
                    }
                }
        } catch (IOException e) {
            System.out.println("Network connection lost: " + e.getMessage());
            // 连接断开时的处理
            handleConnectionLost();
        } catch (Exception e) {
            System.out.println("Error processing server message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 处理连接断开的情况
    private void handleConnectionLost() {
        System.out.println("Connection to server lost. Returning to main menu...");
        try {
            // 关闭资源
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing input stream: " + e.getMessage());
        }

        // 在事件调度线程中更新UI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 关闭当前窗口
                myFrame.dispose();
                // 返回开始界面
                new Client.SurfaceGUI.BeginGUI();
            }
        });
    }

}
