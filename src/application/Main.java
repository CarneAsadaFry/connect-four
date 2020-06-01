package application;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Main extends Application {
	
	final static int MAX_DEPTH = 9;
	final static int THREE_WEIGHT = 100;
	final static int TWO_WEIGHT = 50;
	
	@Override
	public void start(Stage primaryStage) {
		StackPane root = new StackPane();
		BorderPane circles = new BorderPane();
		BorderPane click = new BorderPane();
		root.setStyle("-fx-background-color: #add8e6;");
		Scene scene = new Scene(root, 700, 630);
		
		Button reset = new Button("Reset");
		reset.setTranslateY(295);
		
		Board board = new Board();
		
		int RADIUS = 40;
		for(int i = 1; i <= 7; i++) {
			for(int j = 1; j <= 6; j++) {
				Circle c = new Circle((10 + RADIUS) * 2 * i - 50, (10 + RADIUS) * 2 * j - 50, RADIUS, Color.WHITE);
				circles.getChildren().add(c);
			}
		}
		
		for(int i = 0; i < 7; i++) {
			Rectangle r = new Rectangle(0 + 100 * i, 0, 100, 600);
			r.setFill(Color.color(1, 0, 0, 0));
			r.setOnMouseClicked(new ButtonHandler(i, board, circles));
			click.getChildren().add(r);
		}
		
		reset.setOnAction(e -> {
			board.state = new Chip[6][7];
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 7; j++) {
					board.state[i][j] = Chip.NONE;
				}
			}
			board.update(circles);
			board.redTurn = true;
		});
		
		root.getChildren().addAll(circles, click, reset);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("Deep Casey v1.4.2");
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	static class Board {		
		Chip[][] state = new Chip[6][7];
		boolean redTurn = true;
		
		Board() {
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 7; j++) {
					state[i][j] = Chip.NONE;
				}
			}
		}
		
		Board(Chip[][] state){
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 7; j++) {
					this.state[i][j] = state[i][j];
				}
			}
		}
		
		Chip checkWin(Chip[][] state) {
			//Check for draw
			boolean win = true;
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 7; j++) {
					if(state[i][j] == Chip.NONE)
						win = false;
				}
			}
			if(win) return Chip.TIE;
			
			//Check for vertical (from top)
			for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 7; j++) {
					if(state[i][j] != Chip.NONE) {
						Chip color = state[i][j];
						win = true;
						for(int k = 1; k < 4; k++) {
							if(state[i + k][j] != color) {
								win = false;
								break;
							}
						}
						if(win) return color;
					}
				}
			}
			//Check for horizontal (from left)
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 4; j++) {
					if(state[i][j] != Chip.NONE) {
						Chip color = state[i][j];
						win = true;
						for(int k = 1; k < 4; k++) {
							if(state[i][j + k] != color) {
								win = false;
								break;
							}
						}
						if(win) return color;
					}
				}
			}
			//Check for right diagonal
			for(int i = 0; i < 3; i++) {
				for(int j = 3; j < 7; j++) {
					if(state[i][j] != Chip.NONE) {
						Chip color = state[i][j];
						win = true;
						for(int k = 1; k < 4; k++) {
							if(state[i + k][j - k] != color) {
								win = false;
								break;
							}
						}
						if(win) return color;
					}
				}
			}
			//Check for left diagonal
			for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 4; j++) {
					if(state[i][j] != Chip.NONE) {
						Chip color = state[i][j];
						win = true;
						for(int k = 1; k < 4; k++) {
							if(state[i + k][j + k] != color) {
								win = false;
								break;
							}
						}
						if(win) return color;
					}
				}
			}
			return Chip.NONE;
		}
		
		boolean add(Chip chip, int col) {
			for(int i = 5; i >= 0; i--) {
				if(state[i][col] == Chip.NONE) {
					state[i][col] = chip;
					return true;
				}
			}
			return false;
		}
		
		void update(BorderPane pane) {
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 7; j++) {
					if(state[i][j] == Chip.RED)
						((Circle)pane.getChildren().get(i + j * 6)).setFill(Color.RED);
					else if(state[i][j] == Chip.BLACK)
						((Circle)pane.getChildren().get(i + j * 6)).setFill(Color.BLACK);
					else if(state[i][j] == Chip.NONE)
						((Circle)pane.getChildren().get(i + j * 6)).setFill(Color.WHITE);
				}
			}
		}
		
		int play() {
			return minimax(state, 0, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		
		private int minimax(Chip[][] state, int depth, boolean isMax, int alpha, int beta) {
			Chip result = checkWin(state);
			if(result == Chip.RED)
				return -100_000;
			if(result == Chip.BLACK)
				return 100_000 - 100 * depth;
			if(result == Chip.TIE)
				return 0;
			
			if(depth == MAX_DEPTH) {
				return heuristic(state);
			}
			
			if(isMax) {
				int max = Integer.MIN_VALUE;
				int sol = 0;
				for(int i = 0; i < 7; i++) {
					Board temp = new Board(state);
					if(!temp.add(Chip.BLACK, i))
						continue;
					int val = minimax(temp.state, depth + 1, !isMax, alpha, beta);
					if(val > max) {
						max = val;
						sol = i;
					}
					alpha = Math.max(max, alpha);
					
					if(alpha >= beta)
						break;
				}
				if(depth == 0)
					return sol;
				return max;
			} else {
				int min = Integer.MAX_VALUE;
				for(int i = 0; i < 7; i++) {
					Board temp = new Board(state);
					if(!temp.add(Chip.RED, i))
						continue;
					int val = minimax(temp.state, depth + 1, !isMax, alpha, beta);
					min = Math.min(val, min);
					beta = Math.min(min, beta);
					
					if(alpha >= beta)
						break;
				}
				return min;
			}
		}
		
		private int heuristic(Chip[][] state) {
			return (int)(THREE_WEIGHT * (countThree(Chip.BLACK, state) - countThree(Chip.RED, state)) 
					+ TWO_WEIGHT * (countTwo(Chip.BLACK, state) - countTwo(Chip.RED, state)));
		}
		
		private double countThree(Chip color, Chip[][] state) {
			boolean three;
			double score = 0;
			//Check for vertical (from top)
			for(int i = 1; i < 4; i++) {
				for(int j = 0; j < 7; j++) {
					if(state[i][j] == color) {
						three = true;
						for(int k = 1; k < 3; k++) {
							if(state[i + k][j] != color) {
								three = false;
								break;
							}
						}
						if(three) score += 0.0; //prev 0.85
					}
				}
			}
			
			//Check for horizontal (from left)
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 5; j++) {
					if(state[i][j] == color) {
						three = true;
						for(int k = 1; k < 3; k++) {
							if(state[i][j + k] != color) { //CHANGE TO COUNT X XX AND X XX
								three = false;
								break;
							}
						}
						if(three) {
							if((j == 0 || state[i][j - 1] != Chip.NONE) ^ (j == 4 || state[i][j + 3] != Chip.NONE))
								score += 0.8;
							else if(!(j == 0 || state[i][j - 1] != Chip.NONE) && !(j == 4 || state[i][j + 3] != Chip.NONE))
								score++;
						}
					}
				}
			}
			//Check for right diagonal
			for(int i = 0; i < 4; i++) {
				for(int j = 2; j < 7; j++) {
					if(state[i][j] == color) {
						three = true;
						for(int k = 1; k < 3; k++) {
							if(state[i + k][j - k] != color) {
								three = false;
								break;
							}
						}
						if(three) {
							if((i == 0 || j == 6 || state[i - 1][j + 1] != Chip.NONE) 
									^ (i == 3 || j == 2 || state[i + 1][j - 1] != Chip.NONE))
								score += 0.8;
							else if(!(i == 0 || j == 6 || state[i - 1][j + 1] != Chip.NONE) 
									&& !(i == 3 || j == 2 || state[i + 3][j - 3] != Chip.NONE))
								score++;
						}
					}
				}
			}
			//Check for left diagonal
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 5; j++) {
					if(state[i][j] == color) {
						three = true;
						for(int k = 1; k < 3; k++) {
							if(state[i + k][j + k] != color) {
								three = false;
								break;
							}
						}
						if(three) {
							if((i == 0 || j == 0 || state[i - 1][j - 1] != Chip.NONE) 
									^ (i == 3 || j == 4 || state[i + 1][j + 1] != Chip.NONE))
								score += 0.8;
							else if(!(i == 0 || j == 0 || state[i - 1][j - 1] != Chip.NONE) 
									&& !(i == 3 || j == 4 || state[i + 3][j + 3] != Chip.NONE))
								score++;
						}
					}
				}
			}
			return score;
		}
		
		private double countTwo(Chip color, Chip[][] state) {
			boolean two;
			double score = 0;
			//Check for vertical (from top)
			for(int i = 1; i < 5; i++) {
				for(int j = 0; j < 7; j++) {
					if(state[i][j] == color) {
						two = true;
						for(int k = 1; k < 2; k++) {
							if(state[i + k][j] != color) {
								two = false;
								break;
							}
						}
						if(two) score += 0.80;
					}
				}
			}
			
			//Check for horizontal (from left)
			for(int i = 0; i < 6; i++) {
				for(int j = 0; j < 6; j++) {
					if(state[i][j] == color) {
						two = true;
						for(int k = 1; k < 2; k++) {
							if(state[i][j + k] != color) {
								two = false;
								break;
							}
						}
						if(two) {
							if((j == 0 || state[i][j - 1] != Chip.NONE) ^ (j == 5 || state[i][j + 2] != Chip.NONE))
								score += 0.8;
							else if(!(j == 0 || state[i][j - 1] != Chip.NONE) && !(j ==  5|| state[i][j + 2] != Chip.NONE))
								score++;
						}
					}
				}
			}
			//Check for right diagonal
			for(int i = 0; i < 5; i++) {
				for(int j = 1; j < 7; j++) {
					if(state[i][j] == color) {
						two = true;
						for(int k = 1; k < 2; k++) {
							if(state[i + k][j - k] != color) {
								two = false;
								break;
							}
						}
						if(two) {
							if((i == 0 || j == 6 || state[i - 1][j + 1] != Chip.NONE) 
									^ (i == 4 || j == 1 || state[i + 2][j - 2] != Chip.NONE))
								score += 0.8;
							else if(!(i == 0 || j == 6 || state[i - 1][j + 1] != Chip.NONE) 
									&& !(i == 4 || j == 1 || state[i + 2][j - 2] != Chip.NONE))
								score++;
						}
					}
				}
			}
			//Check for left diagonal
			for(int i = 0; i < 5; i++) {
				for(int j = 0; j < 6; j++) {
					if(state[i][j] == color) {
						two = true;
						for(int k = 1; k < 2; k++) {
							if(state[i + k][j + k] != color) {
								two = false;
								break;
							}
						}
						if(two) {
							if((i == 0 || j == 0 || state[i - 1][j - 1] != Chip.NONE) 
									^ (i == 4 || j == 5 || state[i + 2][j + 2] != Chip.NONE))
								score += 0.8;
							else if(!(i == 0 || j == 0 || state[i - 1][j - 1] != Chip.NONE) 
									&& !(i == 4 || j == 5 || state[i + 2][j + 2] != Chip.NONE))
								score++;
						}
					}
				}
			}
			return score;
		}
	}
	
	static enum Chip {
		RED, BLACK, NONE, TIE;
	}
	
	class ButtonHandler implements EventHandler<MouseEvent> {
		int row;
		Board board;
		BorderPane pane;
		
		public ButtonHandler(int row, Board board, BorderPane pane) {
			this.row = row;
			this.board = board;
			this.pane = pane;
		}
		
		@Override
		public void handle(MouseEvent arg0) {
			if(board.redTurn && board.add(Chip.RED, row)) {                	
				board.update(pane);
				board.redTurn = !board.redTurn;
            	new Thread(() -> {
            		Chip winner = board.checkWin(board.state);
    				if(winner == Chip.TIE) {
    					JOptionPane.showMessageDialog(null, "TIE!");
    				} else if(winner != Chip.NONE) {
    					JOptionPane.showMessageDialog(null, winner + " IS THE WINNER!!!"); 
    				}
    				
    				int move = board.play();
    				if(move >= 0 && move < 7)
    					board.add(Chip.BLACK, move);
    				board.update(pane);
    				winner = board.checkWin(board.state);
    				if(winner == Chip.TIE) {
    					JOptionPane.showMessageDialog(null, "TIE!");
    					board.redTurn = !board.redTurn;
    				} else if(winner != Chip.NONE) {
    					JOptionPane.showMessageDialog(null, winner + " IS THE WINNER!!!"); 
    					board.redTurn = !board.redTurn;
    				}
    				board.redTurn = !board.redTurn;
            	}).start();
			}
		}
	}
}
