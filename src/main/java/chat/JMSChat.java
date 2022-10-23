package chat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JMSChat  extends Application {
	private MessageProducer messageProducer;
    private Session session;
	private String codeUser;

	public static void main(String[] args) {
		Application.launch(JMSChat.class);

	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("JMS chat");
		BorderPane borderPane=new BorderPane();//conteneur qui permet de disposer de 5 zone 
		HBox hBox =new HBox();
		hBox.setPadding(new Insets(15));
		hBox.setSpacing(10);
		hBox.setBackground(new Background(
				new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));
		
		Label labelCode=new Label("Code : ");
		labelCode.setPadding(new Insets(5));
		TextField textFieldCode= new TextField("C1");
		textFieldCode.setPromptText("Code ");
		
		Label labelHost=new Label("Host : ");
		labelHost.setPadding(new Insets(5));
		TextField textFieldHost= new TextField("localhost");
		textFieldHost.setPromptText("Host ");
		
		Label labelPort=new Label("Port : ");
		labelPort.setPadding(new Insets(5));
		TextField textFieldPort= new TextField("61616");
		textFieldPort.setPromptText("Port ");
		
		Button buttonConnecter=new Button("Connecter");
		hBox.getChildren().add(labelCode);
		hBox.getChildren().add(textFieldCode);
		
		hBox.getChildren().add(labelHost);
		hBox.getChildren().add(textFieldHost);
		
		hBox.getChildren().add(labelPort);
		hBox.getChildren().add(textFieldPort);
		
		hBox.getChildren().add(buttonConnecter);
		
		borderPane.setTop(hBox);
		//***************************************************************
		VBox vBox=new VBox();
		GridPane gridPane=new GridPane();
		HBox hBox2=new HBox();
		
		vBox.getChildren().add(gridPane);
		vBox.getChildren().add(hBox2);
		borderPane.setCenter(vBox);
		
		Label labelTo=new Label("To :");
		TextField textFieldTo=new TextField("C1");
		
		
		Label labelMessage=new Label("Message :");
		TextArea textAreaMessage =new TextArea();
		
		Button buttonEnvoyer=new Button("Envoyer");
		
		Label labelImage=new Label("image");
		File f =new File("images");
		ObservableList< String>observableListImage=
				FXCollections.observableArrayList(f.list());
		ComboBox<String>comboBoxImages=new ComboBox<String>(observableListImage);
		comboBoxImages.getSelectionModel().select(0);
		Button buttonEnvoyerImage=new Button("Envoyer Image");
		
		gridPane.setPadding(new Insets(15));
		textAreaMessage.setPrefRowCount(1);
		gridPane.setVgap(10);
		gridPane.setHgap(10);
		
		gridPane.add(labelTo, 0, 0);
		gridPane.add(textFieldTo, 1, 0);
		gridPane.add(labelMessage, 0, 1);
		gridPane.add(textAreaMessage, 1, 1);
		gridPane.add(buttonEnvoyer, 2, 1);
		gridPane.add(labelImage, 0, 2);
		gridPane.add(comboBoxImages, 1, 2);
		gridPane.add(buttonEnvoyerImage, 2, 2);
		
		ObservableList<String>observableListMessage=FXCollections.observableArrayList();
		ListView<String> listViewMessage=new ListView<>(observableListMessage);
		
		File f2=new File("images/"+comboBoxImages.getSelectionModel().getSelectedItem());
		Image image=new Image(f2.toURI().toString());
		ImageView imageView=new ImageView(image);
		imageView.setFitWidth(320);
		imageView.setFitHeight(250);
		
		
		hBox2.getChildren().add(listViewMessage);
		hBox2.getChildren().add(imageView);
		hBox2.setPadding(new Insets(10));
		hBox2.setSpacing(10);

		//********************************************************
		Scene scene=new Scene(borderPane,800,500); 
		primaryStage.setScene(scene);
		primaryStage.show();
		
		comboBoxImages.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<String>() {

					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
						
						File f3=new File("images/"+newValue);
						Image image=new Image(f3.toURI().toString());
						imageView.setImage(image);
					}
				});
				
		buttonEnvoyer.setOnAction(e->{
			try {
				TextMessage textMessage=session.createTextMessage();
				textMessage.setText(textAreaMessage.getText());
				textMessage.setStringProperty("code", textFieldTo.getText());
				messageProducer.send(textMessage);
			} catch (JMSException e1) {
				e1.printStackTrace();
			}
		});
		
		buttonEnvoyerImage.setOnAction(e->{
			
				try {
					StreamMessage streamMessage=session.createStreamMessage();
					streamMessage.setStringProperty("code", textFieldTo.getText());
					File f4=new File("images/"+comboBoxImages.getSelectionModel().getSelectedItem());
					FileInputStream fis=new FileInputStream(f4);
					byte[]data=new byte[(int)f4.length()];
					fis.read(data);
					streamMessage.writeString(comboBoxImages.getSelectionModel().getSelectedItem());
					streamMessage.writeInt(data.length);
					streamMessage.writeBytes(data);
					messageProducer.send(streamMessage);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		});
		
		buttonConnecter.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				codeUser =textFieldCode.getText();
				String host =textFieldHost.getText();
				int port = Integer.parseInt(textFieldPort.getText());
				
				try {
					ConnectionFactory connectionFactory = new 
							ActiveMQConnectionFactory("tcp://"+host+":"+port);
					Connection connection=connectionFactory.createConnection();
					connection.start();
					session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					Destination destination=session.createTopic("file.chat");
					MessageConsumer messageConsumer=session.createConsumer(destination,"code='"+codeUser+"'");
					messageProducer=session.createProducer(destination);
					messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
					messageConsumer.setMessageListener(message->{
							try { 
								if (message instanceof TextMessage) {
								TextMessage textMessage =(TextMessage)message;
								observableListMessage.add(textMessage.getText());
								}
								else if(message instanceof StreamMessage) {
									StreamMessage streamMessage=(StreamMessage)message;
									String nomPhoto=streamMessage.readString();
									observableListMessage.add("Reception d'une photo : "+nomPhoto);
									int size=streamMessage.readInt();
							
									byte[]data=new byte[size];
									streamMessage.readBytes(data);
									
									ByteArrayInputStream arrayInputStream=new ByteArrayInputStream(data);
									Image image=new Image(arrayInputStream);
									imageView.setImage(image);
									
								}
								
							} catch (JMSException e) {
								e.printStackTrace();
							}
						
					});
					
					hBox.setDisable(true);
				} catch (JMSException e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}

}
