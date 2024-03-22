package pacman_game;

import javafx.fxml.FXML;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;

import java.util.*;


public class PacMan_Controller implements EventHandler<KeyEvent> {
    final private static double FRAMES_PER_SECOND = 5.0;
    /**
     * set game levels in .txt files read it one by one when level up
     */
    private static final String[] levelFiles = {"src/pacman_levels/level1.txt", "src/pacman_levels/level2.txt", "src/pacman_levels/level3.txt"};
    @FXML
    private Label scoreLabel;
    @FXML
    private Label levelLabel;
    /**
     * use imageView for display the life count and game over.. message and you won message
     */
    @FXML
    private ImageView imageView;
    @FXML
    private PacMan_View pacManView;
    /**
     * life count 0 is have 3 life
     * life count 1 is have 2 life
     * life count 2 is have 1 life
     * life count 3 is no another life
     */
    private int life_count = 0;
    private PacMan_Model pacManModel;
    private Timer timer;
    private static int ghostEatingModeCounter;
    private boolean paused;

    public PacMan_Controller() {
        /**
         * paused false is game is no start yet
         * true is game is started
         * use is variable when load the game not start automatically,when click a any arrow key the game is starting
         */
        this.paused = false;
    }

    /**
     * Initialize and update the model and view from the first txt file and starts the timer.
     */
    public void initialize() throws InterruptedException {
        String file = this.getLevelFile(0);
        this.pacManModel = new PacMan_Model();
        this.update_directions(PacMan_Model.Direction.NONE);
        ghostEatingModeCounter = 25;
        life_count = 0;
        this.startTimer();
        /**
         when start the game has three life counts for play the game
         */
        if (life_count == 0) {
            Image image = new Image("pacman_images/three-life.png");
            imageView.setImage(image);
        }
    }
    /**
     * Pacman_Model to update based on the timer.
     */
    private void startTimer() {
        this.timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        try {
                            update_directions(pacManModel.getCurrentDirection());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        };
        long frameTimeInMilliseconds = (long) (1000.0 / FRAMES_PER_SECOND);
        this.timer.schedule(timerTask, 0, frameTimeInMilliseconds);
        life_count = 0;
    }
    /**
     * updates the view, updates score and level,displays the life counts , displays Game Over/You Won messages.
     */
    private void update_directions(PacMan_Model.Direction direction) throws InterruptedException {
        this.pacManModel.step(direction);
        try {
            this.pacManView.update(pacManModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * update the Game Level and Score From Here
         */
        this.levelLabel.setText(String.format("Level: %d", this.pacManModel.getLevel()));
        this.scoreLabel.setText(String.format("Score: %d", this.pacManModel.getScore()));
        /**
         * check game over or not to continue the game or display the messages and life counts..................
         */
        if (pacManModel.isGameOver()) {
            pacManModel.life();
            life_count++;
            if (life_count == 2) {
                Image image = new Image("pacman_images/one-life.png");
                imageView.setImage(image);
            }
            if (life_count == 1) {
                Image image = new Image("pacman_images/two-life.png");
                imageView.setImage(image);
            }
            if (life_count == 0) {
                Image image = new Image("pacman_images/three-life.png");
                imageView.setImage(image);
            }
            if (life_count == 3) {
                Image image = new Image("pacman_images/game-over.png");
                imageView.setImage(image);
                pause();
            }
        }
        if (pacManModel.isYouWon()) {
            Image image = new Image("pacman_images/you-won.png");
            imageView.setImage(image);
        }
        /**
         * ghostEatingMode / count down the ghostEatingModeCounter to reset ghostEatingMode to false
         * when the counter is 0
         */
        if (pacManModel.isGhostEatingMode()) {
            ghostEatingModeCounter--;
        }
        if (ghostEatingModeCounter == 0 && pacManModel.isGhostEatingMode()) {
            pacManModel.setGhostEatingMode(false);
        }
    }

    /**
     *  this method handle key board acton when press the keys
     */
    @Override
    public void handle(KeyEvent keyEvent) {
        boolean keyRecognized = true;
        KeyCode code = keyEvent.getCode();
        PacMan_Model.Direction direction = PacMan_Model.Direction.NONE;
        /**
         * when press the left side arrow key pac man go to left side
         */
        if (code == KeyCode.LEFT) {
            direction = PacMan_Model.Direction.LEFT;
            /**
             * when press the right side arrow key pac man go to right side
             */
        } else if (code == KeyCode.RIGHT) {
            direction = PacMan_Model.Direction.RIGHT;
            /**
             * when press the up side arrow key pac man go to up side
             */
        } else if (code == KeyCode.UP) {
            direction = PacMan_Model.Direction.UP;
            /**
             * when press the down side arrow key pac man go to down side
             */
        } else if (code == KeyCode.DOWN) {
            direction = PacMan_Model.Direction.DOWN;
            /**
             * when press the s  key , we can start new game
             * you can change it replacing the this S , you can put another letter
             */
        } else if (code == KeyCode.S) {
            /**
             * when press the s key set three life count
             */
            Image image = new Image("pacman_images/three-life.png");
            imageView.setImage(image);
            pause();
            this.pacManModel.startNewGame();
            paused = false;
            this.startTimer();
        } else {
            keyRecognized = false;
        }
        if (keyRecognized) {
            keyEvent.consume();
            pacManModel.setCurrentDirection(direction);
        }
    }
    public static void setGhostEatingModeCounter() {
        ghostEatingModeCounter = 25;
    }

    public static int getGhostEatingModeCounter() {
        return ghostEatingModeCounter;
    }

    public static String getLevelFile(int x) {
        return levelFiles[x];
    }

    public boolean getPaused() {
        return paused;
    }
    public void pause() {
        this.timer.cancel();
        this.paused = true;
    }

    public double getBoardWidth() {
        return PacMan_View.CELL_WIDTH * this.pacManView.getColumnCount();
    }

    public double getBoardHeight() {
        return PacMan_View.CELL_WIDTH * this.pacManView.getRowCount();
    }
}
