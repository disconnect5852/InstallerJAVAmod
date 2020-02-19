import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;


public class Installer { // all secret are replaced with random numbers, characters
	private String email;
	private String password;
	private String destination;
	private static final boolean DEBUG=true;
	private static final String FOU="http://www.site.com/brand/"; //site of the DB and files
	private static String DB =FOU+"database/z7q581.zdb"; //db file relative path
	private static String OPTION =FOU+"databas/choice.list"; //optional selector list source
	private static final String ORG="2852503222"; //distractor string
	private static final String ALT="651862163"; //distractor string
	private static final String DB2 =FOU+"index.php?reginstall="; //register installation with ip address, log to mysql db table
	private static final byte[] iv = new byte[] 
			{ 
		0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f 
			};
	private static final byte[] ctzl={1,2,3,4,5,6,7,8,7,10,11,22,46}; //WAS db passphrase in char array to make it less visible in bytecode
	private static final byte[] vrtML={150,12,87,74,27,2,12,22,46,32,56,37,100,7,27,16,62,101,38,29,17,31,118,60,101,32,5,38,18,16,97,37,18,22,55,101,36,38,53,23,59,2,96,53,53,54,35,18,37,62,44,17,31,36,101,56,51,102,33,26,30,34,46,109,14,21,39,60,27,7,38,28,49,18,37,24,60,12,100,56,13,53,58,6,44,18,160,37,21,45,31,25,7,71,45,50,53,56,53,38}; //fake, distractor passphrase array
	private static final byte[] replace=new byte[]{97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97,97}; //chars where watermark should be placed
	private static final byte[] salt ={18,-12,-45,88,123,1,127,12,45,5,89,61,-24,-65,34,31}; 
	private static final byte[] replace1 =new byte[]{98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98,98}; //chars where second watermark should be placed
	private static final String TITLE="Le installer(date: 2150-05-01)";
	private static final Frame FRM= new Frame(TITLE);;
	private static URL mainhashlink;
	private String ass;
	private byte[] ajdi;
	private byte[] ajdi2;
	private static final int TRANSFER_BLOCK=8388608;
	private final Panel panel = new Panel();
	private static final Label progress = new Label("");
	private Cipher dcipher;
	private static List<URL> options=new ArrayList<URL>();
	private static final java.awt.List CHOICE=new java.awt.List(2);
	private static final Checkbox CHECKBOX= new Checkbox("Check/Download ONLY the selected optional contents");
	private static int counter=0;
	private static boolean alternative=false;


	private Installer() {
		super();
		panel.setBackground(Color.LIGHT_GRAY);
		FRM.add(panel);
		//gridro++;
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {260, 260};
		gbl_panel.rowHeights = new int[] {34, 34, 34, 34, 50};
		gbl_panel.columnWeights = new double[]{0, 1};
		gbl_panel.rowWeights = new double[]{0, 0, 0, 1, 1};
		panel.setLayout(gbl_panel);

		Label label = new Label("Email address which you used for donation");
		//label.setMaximumSize(new Dimension(32767, 10));
		label.setAlignment(Label.RIGHT);
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.fill = GridBagConstraints.BOTH;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		panel.add(label, gbc_label);

		final TextField gemail = new TextField();
		gemail.setColumns(20);
		GridBagConstraints gbc_gemail = new GridBagConstraints();
		gbc_gemail.fill = GridBagConstraints.BOTH;
		gbc_gemail.insets = new Insets(0, 0, 5, 0);
		gbc_gemail.gridx = 1;
		gbc_gemail.gridy = 0;
		panel.add(gemail, gbc_gemail);

		Label label_1 = new Label("Password of your donation");
		label_1.setAlignment(Label.RIGHT);
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.fill = GridBagConstraints.BOTH;
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 0;
		gbc_label_1.gridy = 1;
		panel.add(label_1, gbc_label_1);
		Button apply = new Button("Check/Download");
		final TextField gepassword = new TextField();
		apply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				counter=0;
				email=gemail.getText().trim();
				password=gepassword.getText().trim();
				if (!isEmptyOrBlank(email) && !isEmptyOrBlank(password)) {
					FileDialog file= new FileDialog(FRM,"Please locate your game installation by finding and selecting game.exe", FileDialog.LOAD);
					file.setFile("game.exe");
					file.setVisible(true);
					String fl= file.getFile();
					destination=file.getDirectory();
					if (fl!=null && fl.equalsIgnoreCase("game.exe") && new File(destination+fl).exists()) {
						Thread t= new Thread(new LetsDoThis());
						t.start();
					} else {
						progress.setText("You need to select game.exe!");
						progress.setBackground(Color.YELLOW);
					}
				} else {
					progress.setBackground(Color.YELLOW);
					progress.setText("The email address in incorrect or the password is missing");
				}
			}
		});

		gepassword.setColumns(20);
		GridBagConstraints gbc_gepassword = new GridBagConstraints();
		gbc_gepassword.fill = GridBagConstraints.BOTH;
		gbc_gepassword.insets = new Insets(0, 0, 5, 0);
		gbc_gepassword.gridx = 1;
		gbc_gepassword.gridy = 1;
		panel.add(gepassword, gbc_gepassword);
		GridBagConstraints gbc_apply = new GridBagConstraints();
		gbc_apply.fill = GridBagConstraints.BOTH;
		gbc_apply.insets = new Insets(0, 0, 5, 5);
		gbc_apply.gridx = 0;
		gbc_apply.gridy = 2;
		panel.add(apply, gbc_apply);
		progress.setFont(new Font("Arial", Font.PLAIN, 9));

		GridBagConstraints gbc_progress = new GridBagConstraints();
		gbc_progress.fill = GridBagConstraints.BOTH;
		gbc_progress.insets = new Insets(0, 0, 5, 0);
		gbc_progress.gridx = 1;
		gbc_progress.gridy = 2;
		panel.add(progress, gbc_progress);

		Label label_2 = new Label("Select optional downloads:");
		label_2.setAlignment(Label.RIGHT);
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.fill = GridBagConstraints.BOTH;
		gbc_label_2.insets = new Insets(0, 0, 5, 5);
		gbc_label_2.gridx = 0;
		gbc_label_2.gridy = 3;
		panel.add(label_2, gbc_label_2);
		CHOICE.setMultipleMode(true);
		GridBagConstraints gbc_CHOICE = new GridBagConstraints();
		gbc_CHOICE.gridheight = 2;
		gbc_CHOICE.fill = GridBagConstraints.BOTH;
		gbc_CHOICE.gridx = 1;
		gbc_CHOICE.gridy = 3;
		panel.add(CHOICE, gbc_CHOICE);
		GridBagConstraints gbc_CHECKBOX = new GridBagConstraints();
		gbc_CHECKBOX.fill = GridBagConstraints.BOTH;
		gbc_CHECKBOX.insets = new Insets(0, 0, 0, 5);
		gbc_CHECKBOX.gridx = 0;
		gbc_CHECKBOX.gridy = 4;
		panel.add(CHECKBOX, gbc_CHECKBOX);
		//gridco++;

		try {
			checkChoices();
		} catch (Exception e) {
 */			if(DEBUG) {
				e.printStackTrace();
			}
			//System.out.println("hoppávan! fallbackelés is van");
			DB="https://www.dropbox.com/s/r4l1uucppt2r6z8/z7q581.zdp?dl=1";
			OPTION="https://www.dropbox.com/s/c9kaiurdkpfe58o/choice.list?dl=1";
			alternative=true;
			try {
				checkChoices();
			} catch (Exception e1) {
				if(DEBUG) {
					e.printStackTrace();
				}
				progress.setBackground(Color.RED);
				progress.setText("Database files are unreachable, check your connection, and firewall, or update the Installer if not up to date");
			}
		};
		//Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		//frm.setSize(dim.width/2,dim.height/2);
		FRM.setVisible(true);
		FRM.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we)
			{
				System.exit(0);
				/*if (inProgress) {
					
				} else {
					System.exit(0);
				}*/
			}
		});
		FRM.pack();
	}
	private static boolean isEmptyOrBlank(String str) {
		return str == null || str.isEmpty();
	}
	private void checkChoices() throws Exception {
			BufferedReader opts= new BufferedReader(new InputStreamReader(new URL(OPTION).openStream()));
			String rawline;
			while ((rawline = opts.readLine()) != null) {
				String[] line= rawline.split(";");
				//System.out.println(rawline);
				options.add(new URL(line[1]));
				//CHOICE.setMultipleSelections(true);
				CHOICE.add(line[0]);
			}
			opts.close();
	}
	private boolean isLegit() throws MalformedURLException, IOException, FileNotFoundException {
		setMausoleum();
		
		DataInputStream dis= new DataInputStream(new InflaterInputStream(deMausoleum(new BufferedInputStream(new URL(DB).openStream())), new Inflater()));
		Files.copy(new InflaterInputStream(deMausoleum(new BufferedInputStream(new URL(DB).openStream())), new Inflater()), FileSystems.getDefault().getPath("f:/mausoleum", "access.log"), StandardCopyOption.REPLACE_EXISTING);
			mainhashlink=new URL(dis.readUTF());
		ass=dis.readUTF();
		try {
			while(true) {
				String[] row={dis.readUTF(),dis.readUTF(),dis.readUTF(),dis.readUTF()};
				dis.readBoolean();
				//System.out.println(email.trim() + " "+ password.trim());
				//System.out.println( row[0]+row[1]);
				if (row[0].equalsIgnoreCase(email) && row[1].equalsIgnoreCase(password)) {
					if (row[2].equals("wrz")) {
						removeRecursive(Paths.get(destination+System.getProperty("file.separator")+"Assets"+System.getProperty("file.separator")+"Dev"+System.getProperty("file.separator")+"Addon"));
						return false;
					}
					ajdi=row[2].getBytes();
					ajdi2=row[3].getBytes();
					try {
						dis.close();
					} catch (Exception e) {
						//e.printStackTrace();
					}
					progress.setText("Access granted, checking files");
					progress.setBackground(Color.GREEN);
					try {
						new URL(DB2+row[3]).openStream();
					} catch (Exception e) {
						if(DEBUG) {
							e.printStackTrace();
						}
					}
					dis.close();
					return true;
				}
			}
		} catch (Exception e) {
			if(DEBUG) {
				e.printStackTrace();
			}
			dis.close();
			return false;
		}
	}
	
	private static void removeRecursive(Path path) throws IOException
	{
		Files.walkFileTree(path, new SimpleFileVisitor<Path>()
				{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					throws IOException
					{
				Files.delete(file);
				return FileVisitResult.CONTINUE;
					}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
			{
				// try to delete the file anyway, even if its attributes could not be read
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
			{
				if (exc == null)
				{
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
				else
				{
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
				});
	}
	private Path downloadZip(URL down) throws Exception {
		if (DEBUG) {
			System.out.println(down.toString());
		}
		/*progress.setText("Access granted, initializing download");
		progress.setBackground(Color.GREEN);*/
		//File dafile= File.createTempFile("inst", "cro");
		Path tempfile= Files.createTempFile("inst", "cro");
		//dafile.deleteOnExit();
		//System.out.println(dafile.getName());
		//URL website = new URL(ziplink);
		ReadableByteChannel rbc = Channels.newChannel(/*new XorInputStream(new FileInputStream("e:/Locucc/q37eeritunqssw.cre")*/ down.openStream());//);
		FileChannel fch=FileChannel.open(tempfile, StandardOpenOption.WRITE);
		long bytestransferred= fch.transferFrom(rbc, 0, TRANSFER_BLOCK);
		long totalbytestransferred=0;
		//progress.setBackground(Color.GREEN);
		while (bytestransferred==TRANSFER_BLOCK) {
			totalbytestransferred=totalbytestransferred+bytestransferred;
			//progress.setText("Downloaded: "+totalbytestransferred/1048576f+" mbytes");
			bytestransferred= fch.transferFrom(rbc, totalbytestransferred, TRANSFER_BLOCK);
		}
		fch.close();
		return tempfile;
	}
	private void extractFiles(Path zipfile, String relativepath) throws Exception {
		progress.setBackground(Color.GREEN);
		progress.setText("Extracting files");
		ZipInputStream is = null;
		FileOutputStream os = null;

		// Initiate the ZipFile
		ZipFile zipFile = new ZipFile(zipfile.toFile());
		if (zipFile.isEncrypted()) {
			zipFile.setPassword(ass);
		}

		//Get a list of FileHeader. FileHeader is the header information for all the
		//files in the ZipFile

		@SuppressWarnings("rawtypes")
		List fileHeaderList = zipFile.getFileHeaders();

		// Loop through all the fileHeaders
		for (int i = 0; i < fileHeaderList.size(); i++) {
			FileHeader fileHeader = (FileHeader)fileHeaderList.get(i);
			if (fileHeader != null) {
				String outFilePath;
				if (relativepath==null) {
					outFilePath = destination + System.getProperty("file.separator") + fileHeader.getFileName();
				} else {
					outFilePath=destination+ /*System.getProperty("file.separator")+*/relativepath;
				}
				//System.out.println("Extracting file to:"+outFilePath);
				File outFile = new File(outFilePath);

				if (fileHeader.isDirectory()) {
					outFile.mkdirs();
					continue;
				}
				File parentDir = outFile.getParentFile();
				if (!parentDir.exists()) {
					parentDir.mkdirs();
				}
				is = zipFile.getInputStream(fileHeader);

				os = new FileOutputStream(outFile);

				ReadableByteChannel ch=Channels.newChannel(is);
				if (fileHeader.getFileName().toLowerCase().contains(".geopcdx")) {
					ByteBuffer bbuf = ByteBuffer.allocate((int)fileHeader.getUncompressedSize());
					int read=ch.read(bbuf);
					//byte[] buff = new byte[(int)fileHeader.getUncompressedSize()];
					//StringBuilder strb= new StringBuilder();
					//readLen = is.read(buff); 
					byte[] buff= bbuf.array();
					//System.out.println("read: "+read+" buff:"+buff.length+ " header:"+fileHeader.getUncompressedSize());
					if (1<read) {
						buff=binaryReplace(buff, replace, ajdi, replace1, ajdi2);
						if (buff==null) {
							progress.setBackground(Color.RED);
							progress.setText("geo file extraction failed");
						} else {
							os.write(buff);
						}
					}

				} else {
					os.getChannel().transferFrom(Channels.newChannel(is), 0, Long.MAX_VALUE);
				}
				closeFileHandlers(is, os);
			} else {
				System.err.println("fileheader is null. Shouldn't be here");
			}
		}

	}

	private void closeFileHandlers(ZipInputStream is, OutputStream os) throws IOException{
		if (os != null) {
			os.close();
			os = null;
		}
		//Closing inputstream also checks for CRC of the the just extracted file.
		if (is != null) {
			is.close(true);
			is = null;
		}

	}
	private class FileCheckResult {
		URL url;
		String relativepath;
		private FileCheckResult(final URL url, final String relativepath) {
			super();
			this.url = url;
			this.relativepath = relativepath;
		}

	}
	private class DownloadTask implements Runnable {
	    private FileCheckResult downloadable;

	    public DownloadTask(FileCheckResult downloadable) {
	        this.downloadable = downloadable;
	    }

	    @Override
	    public void run() {
				try {
					Path fil= downloadZip(downloadable.url);
					extractFiles(fil,downloadable.relativepath);
					Files.delete(fil);
					counter++;
					progress.setText(Integer.toString(counter)+" files are installed");
		
				} catch (IOException e) {
					progress.setBackground(Color.RED);
					progress.setText("I/O error: "+e.getClass()+e.getMessage());
					if (DEBUG) {
						e.printStackTrace();
					}

				} catch (Exception e) {
					progress.setBackground(Color.RED);
					progress.setText(e.getClass()+e.getMessage());
					if (DEBUG) {
						e.printStackTrace();
					}
				}
	    }
	}
	private class LetsDoThis implements Runnable {

		@Override
		public void run() {
			try {
				FRM.setTitle(TITLE+" (downloading...)");
				//inProgress=true;
				if (isLegit()) {
					//Set<Path> filz= new HashSet<Path>();
					//int numfiles;
					if (!CHECKBOX.getState()) {
						List<FileCheckResult> mainfiles= checkfiles(mainhashlink);
						counter=0;
						//numfiles=mainfiles.size();
						ExecutorService pool = Executors.newFixedThreadPool(10);
						for (FileCheckResult dwn: mainfiles ) {
							/*Path fil= downloadZip(dwn.url);
							//System.out.println("Downloaded:"+fil);
							extractFiles(fil,dwn.relativepath);
							Files.delete(fil);
							counter++;
							progress.setText(counter+" of "+numfiles);*/
							    pool.submit(new DownloadTask(dwn));
						}
						pool.shutdown();
						pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
						progress.setText("Main files are installed successfully!");
					}
					int[] selz= CHOICE.getSelectedIndexes();
					for (int sel:selz) {
						List<FileCheckResult> optfiles= checkfiles(options.get(sel));
						counter=0;
						//numfiles=+optfiles.size();
						ExecutorService pool = Executors.newFixedThreadPool(10);
						for (FileCheckResult dwn: optfiles) {
							/*Path fil=downloadZip(dwn.url);
							//System.out.println("Downloaded:"+fil);
							extractFiles(fil,dwn.relativepath);
							Files.delete(fil);
							counter++;
							progress.setText(counter+" of ");*/
							pool.submit(new DownloadTask(dwn));
						}
						pool.shutdown();
						pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
						progress.setBackground(Color.GREEN);
						progress.setText("An optional package is installed successfully!");
					}
					
					//extractFiles(fil);
					//Files.delete(fil);
					ass="FDFDFERE%EF23721873878d88728z17241782z4z87d78w8dz723";
					progress.setBackground(Color.GREEN);
					progress.setText("All downloads are completed!");
					//inProgress=false;
				} else {
					//throw new Exception("Access denied. Check your password and email. If you've donated already please give us some time to process your donation.");
					progress.setBackground(Color.RED);
					progress.setText("Access denied. Check your password and email.");
				}
			} catch (FileNotFoundException e) {
				progress.setBackground(Color.RED);
				progress.setText("Database files are unreachable, check your connection, and firewall, or update this installer (http://dl.com/thisthing.zip)");
				if(DEBUG) {
					e.printStackTrace();
				}
			} catch (net.lingala.zip4j.exception.ZipException e) {
				progress.setBackground(Color.RED);
				progress.setText("Data file is corrupted");
				if(DEBUG) {
					e.printStackTrace();
				}
			} catch (java.util.zip.ZipException e) {
				progress.setBackground(Color.RED);
				progress.setText("Database file is corrupted");
				if(DEBUG) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				progress.setBackground(Color.RED);
				progress.setText(e.getClass()+e.getMessage());
				if (DEBUG) {
					e.printStackTrace();
				}
			}
			FRM.setTitle(TITLE);
		}

	}
	private List<FileCheckResult> checkfiles(URL hashfile) throws Exception {
		final String space=" ";
		counter=0;
		progress.setBackground(Color.GREEN);
		List<FileCheckResult> filelist = new ArrayList<FileCheckResult>();
		BufferedReader md5s= new BufferedReader(new InputStreamReader(hashfile.openStream()));
		String rootofhash=hashfile.toString();
		rootofhash=rootofhash.substring(0, rootofhash.lastIndexOf('/')+1);
		String rawline;
		MessageDigest md = MessageDigest.getInstance("MD5");
		while ((rawline = md5s.readLine()) != null) {
			String[] line= rawline.split(space,2);
			//System.out.println(hashfile+"  "+rawline);
			String furl= line[1].replaceFirst("\\*", "");
			Path chkfil= Paths.get((destination+furl));
			if (Files.exists(chkfil)) {
				md.reset();
				byte [] fileData =Files.readAllBytes(chkfil);
				if (chkfil.toString().toLowerCase().contains(".geopcdx")) {
					fileData= binaryReplace(fileData, ajdi, replace, ajdi2, replace1);
				}
				counter++;
				progress.setText(counter+" files checked");
				
				if (line[0].equalsIgnoreCase(byteArrayToHex(md.digest(fileData)))) {
					continue;
				}
				//System.out.println("not match");
			}
			//System.out.println(rootofhash+" "+furl);
			//System.out.println("will be downloaded :"+chkfil);
			filelist.add(new FileCheckResult(new URL(rootofhash+furl/*.substring(0, furl.lastIndexOf("."))*/.replace('\\', '/').replace(space, "%20")+".zip"), furl));
		}
		return filelist;
	}

	private static byte[] binaryReplace(byte[] input, byte[] search, byte[] replacebyte, byte[] search2, byte[] replacebyte2) {
		int index=indexOf(input, search);
		int index1=indexOf(input, search2);
		if (index>0){
			for (int j=0; j<replacebyte.length; j++) {
				input[index+j]=replacebyte[j];

			}
			if (index1>0) {
				for (int j=0; j<replacebyte2.length; j++) {
					input[index1+j]=replacebyte2[j];
				}
			}
			return input;
		}
		return null;
	}
	
	private static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder();
		for(byte b: a)
			sb.append(String.format("%02x", b&0xff));
		return sb.toString();
	}
	
	private static int indexOf(byte[] data, byte[] pattern) {
		int[] failure = computeFailure(pattern);

		int j = 0;

		for (int i = 0; i < data.length; i++) {
			while (j > 0 && pattern[j] != data[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == data[i]) {
				j++;
			}
			if (j == pattern.length) {
				return i - pattern.length + 1;
			}
		}
		return -1;
	}


	private static int[] computeFailure(byte[] pattern) {
		int[] failure = new int[pattern.length];

		int j = 0;
		for (int i = 1; i < pattern.length; i++) {
			while (j>0 && pattern[j] != pattern[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == pattern[i]) {
				j++;
			}
			failure[i] = j;
		}

		return failure;
	}

	private void setMausoleum() {
		byte cvwm= (byte)(84);
		byte[] stml  = new byte[vrtML.length];
		System.out.println("Esteemel");
		for (int i=0; i<vrtML.length; i++) {
			stml[i]=(byte) (vrtML[i] ^ cvwm);
			//System.out.println((char)stml[i]);
		}

		AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv); 
		try 
		{
			//rand.nextBytes(salt);  
			//PBEKeySpec password = new PBEKeySpec(passphrase.toCharArray(), new byte[16], 1, 128);  
			//SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			ctzl.toString();
			//System.out.println(new String(stml).toCharArray());
			PBEKey key = (PBEKey) SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new PBEKeySpec(new String(stml).toCharArray(), salt, 7, 128));
			//System.out.println(new String(stml));
			SecretKey encKey = new SecretKeySpec(key.getEncoded(), "AES"); 
			//ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			  /*StringBuilder sb = new StringBuilder();
			    for (Provider b : Security.getProviders()) {
			        sb.append(b.toString());
			        sb.append(System.getProperty("line.separator"));
			    }
			    System.out.println(sb.toString());*/
			dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); 

			dcipher.init(Cipher.DECRYPT_MODE, encKey, paramSpec);
			if (DEBUG) {
			System.out.println( encKey.getAlgorithm());
			  StringBuilder sb = new StringBuilder();
			    for (byte b : encKey.getEncoded()) {
			        sb.append(String.format("%02X ", b));
			    }
			    System.out.println(sb.toString());
			}
		} 
		catch (Exception e) 
		{ 
			if(DEBUG) {
				e.printStackTrace();
			}
		} 
	}
	/*private OutputStream enMausoleum(OutputStream out) {
    	return new CipherOutputStream(out, ecipher);
    }*/
	private InputStream deMausoleum(InputStream in) {
		return new CipherInputStream(in, dcipher);
	}
	public static void main(String[] args) {
		new Installer();
	}

}
