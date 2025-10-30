package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class FighterServer {
    private ServerSocket serverSocket;
    private Vector<ServerThread> threads;
    private final int CLIENT_NUM = 2;
    private volatile boolean serverRunning = true; // 服务器运行状态标志
    /**
     * 装线程，便于管理
     */
    public FighterServer() {
        try {
            //服务端地址
            InetAddress ip4 = Inet4Address.getLocalHost();
            System.out.println("server IP address: " + ip4.getHostAddress());

            System.out.println("Server: Waiting for connection...");
            serverSocket = new ServerSocket(9999);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Didn't listen the port 9999");
            System.exit(1);
        }
        threads = new Vector<ServerThread>();
    }

    /**
     * 将某条消息发布到所有客户端线程中
     * @param s
     */
    public void publish(String s) {
        for(ServerThread tmpThreads: threads) {
            tmpThreads.out.println(s);
            tmpThreads.out.flush();
        }
    }

    /**
     * 两个线程分别与两个客户端建立联系，
     * 读取某一方客户端发来的指令，
     * 输出本线程客户端发送的指令到对方线程的客户端
     */
    class ServerThread extends Thread {//服务器线程
        Socket socket = null;
        private PrintWriter out = null;
        private BufferedReader in = null;
        private int playerNumber;//玩家1在左，玩家2在右

        public ServerThread(Socket s,int playerNumber) {
            this.socket = s;
            this.playerNumber = playerNumber;//线程编号

            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out.println("PLAYER_" + playerNumber);//告诉客户端玩家号

            } catch (IOException e) {
                //TODO: handle exception
                e.printStackTrace();
            }
        }

        //server起到转接的作用，将一个client的位置信息以server为中转发送给另一个client
        @Override
        public void run() {
            String fromUser = null;
            try {
                while ((fromUser = in.readLine()) != null) {

                    System.out.println("Receive: " + fromUser);//收到
                    //TO DO
                    if (fromUser.equals("@EXIT@")) {
                        break;//接收到退出信息
                    }

                    // 检查目标线程是否存在且连接有效后再转发消息
                    if (playerNumber == 1 && threads.size() > 1) {
                        ServerThread targetThread = threads.get(1);
                        if (targetThread != null && !targetThread.socket.isClosed()) {
                            targetThread.out.println(fromUser);
                            targetThread.out.flush();
                        }
                    } else if (playerNumber == 2 && threads.size() > 0) {
                        ServerThread targetThread = threads.get(0);
                        if (targetThread != null && !targetThread.socket.isClosed()) {
                            targetThread.out.println(fromUser);
                            targetThread.out.flush();
                        }
                    }

                    //publish(fromUser);//将指令发送到每一个线程
                }
            } catch (IOException e) {
                // 处理连接重置等网络异常
                System.out.println("Client " + playerNumber + " disconnected unexpectedly: " + e.getMessage());
            } catch (Exception e) {
                // 处理其他可能的异常
                System.out.println("Error processing message from client " + playerNumber + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                // 清理资源
                cleanup();
            }
        }

        // 清理资源的方法
        private void cleanup() {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                
                // 从线程列表中移除当前线程
                synchronized(threads) {
                    threads.remove(this);
                }
                
                System.out.println("Client " + playerNumber + " connection closed!");
                System.out.println("Remaining threads: " + threads.size());
            } catch (IOException ex) {
                System.out.println("Error closing resources for client " + playerNumber + ": " + ex.getMessage());
            }
        }

        //得到当前线程的socket流
        public PrintWriter getOut() {
            return out;
        }
        public BufferedReader getIn() {
            return in;
        }
    }


    public void launchServer() {
        int playerCounter = 1; // 用于分配玩家编号
        
        while(serverRunning) {
            try {
                //只允许两个线程进入
                if(threads.size() < CLIENT_NUM) {
                    System.out.println("Waiting for clients... (" + threads.size() + "/" + CLIENT_NUM + " connected)");
                    
                    // 设置接收连接的超时时间，以便能检查服务器运行状态
                    serverSocket.setSoTimeout(1000); // 1秒超时
                    
                    // 接受新的客户端连接
                    Socket clientSocket = serverSocket.accept();
                    
                    // 分配玩家编号（1或2）
                    int playerNumber = playerCounter;
                    playerCounter = (playerCounter == 1) ? 2 : 1; // 在1和2之间切换
                    
                    ServerThread st = new ServerThread(clientSocket, playerNumber);
                    
                    synchronized(threads) {
                        threads.add(st);
                    }

                    //test
                    System.out.println("Threads: " + threads.size());
                    st.start();

                    System.out.println("Server: Client " + playerNumber + " connected.");
                    
                    // 当两个玩家都连接后，告诉客户端可以进入游戏
                    if (threads.size() == CLIENT_NUM) {
                        publish("ACTION");
                        System.out.println("Both players connected. Game starting...");
                    }
                } else {
                    // 如果已经有两个客户端连接，短暂等待
                    Thread.sleep(100);
                }
            } catch (java.net.SocketTimeoutException e) {
                // 超时是正常的，继续检查服务器状态
                continue;
            } catch (IOException e) {
                if (serverRunning) {
                    System.out.println("Error accepting client connection: " + e.getMessage());
                    e.printStackTrace();
                }
                // 如果服务器已关闭，则退出循环
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Server main loop exited.");
    }


    // 关闭服务器的方法
    public void shutdown() {
        System.out.println("Shutting down server...");
        serverRunning = false;
        
        try {
            // 通知所有客户端服务器即将关闭
            publish("@SERVER_SHUTDOWN@");
            
            // 关闭所有客户端连接
            synchronized(threads) {
                for (ServerThread thread : threads) {
                    try {
                        if (thread.socket != null && !thread.socket.isClosed()) {
                            thread.socket.close();
                        }
                    } catch (IOException e) {
                        System.out.println("Error closing client socket: " + e.getMessage());
                    }
                }
                threads.clear();
            }
            
            // 关闭服务器socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error during server shutdown: " + e.getMessage());
        }
        
        System.out.println("Server shutdown complete.");
    }
    
    public static void main(String[] args) throws IOException {
        FighterServer server = new FighterServer();
        
        // 添加关闭钩子，确保服务器能优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown();
            }
        });
        
        server.launchServer();
    }
}