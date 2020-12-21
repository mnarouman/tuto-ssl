package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class UtilConcatFile {

	public static void main(String args[]) throws Throwable {
		if (args.length < 2) {
			System.out.println(
				"Usage: UtilConcatFile <source> <destination> where\n"
					+ " <source> is the new Trusted certificat\n"
					+ " and <destination> is the other Trusted certificat file.\n");
			System.exit(0);
		}
		String aSource = args[0];
		String aDestination = args[1];
		UtilConcatFile aConcat = new UtilConcatFile();
		aConcat.service(aSource, aDestination);
	}
	public UtilConcatFile() {
		super();
	}

	public void service(String pSource, String pDestination) {
		
		File aSourceFile = new File(pSource);
		File aWorkingDir = aSourceFile.getParentFile();
		String aWorkingFileString = aWorkingDir.getAbsolutePath()+"\\"+"concat.dat";
		
		if (aSourceFile!=null || !aSourceFile.exists()) {
			FileOutputStream aFileOutputStream = null;
			FileInputStream aFileInputStream = null;
			File aDestinationFile = new File(pDestination);
			if (aDestinationFile==null || !aDestinationFile.exists()) {
				try {
					System.out.println("Le fichier "+pDestination+" n'existe pas.");
					System.out.println("  ... on le creer.");
					aDestinationFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Première étape : on copie la destination dans le fichier de travail
			// et on le laisse ouvert pour y ajouter le nouveau :
			try {
				aFileOutputStream = new FileOutputStream(aWorkingFileString);
				aFileInputStream = new FileInputStream(pDestination);
				
			    byte[] buffer = new byte[1024];
			    int length;
			    while ((length = aFileInputStream.read(buffer)) != -1)
			        aFileOutputStream.write(buffer, 0, length);

				aFileInputStream.close();
				byte[] aReturnCady = new byte[2];
				aReturnCady[0]='\r';
				aReturnCady[1]='\n';
		        aFileOutputStream.write(aReturnCady, 0, 2);
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			// Deuxième étape : on ajoute le nouveau :
			try {
				aFileInputStream = new FileInputStream(pSource);	
				
			    byte[] buffer = new byte[1024];
			    int length;
			    while ((length = aFileInputStream.read(buffer)) != -1)
			        aFileOutputStream.write(buffer, 0, length);
	
				aFileInputStream.close();
				aFileOutputStream.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			// Toisième étape : on supprime l'ancien fichier de destination :
			File aDestinationDeleteFile = new File(pDestination);
			aDestinationDeleteFile.delete();
			
			// Quatrième étape : On copie le fichier de travail dans le fichier de destination :
			try {
				aFileOutputStream = new FileOutputStream(pDestination);
				aFileInputStream = new FileInputStream(aWorkingFileString);	
				
			    byte[] buffer = new byte[1024];
			    int length;
			    while ((length = aFileInputStream.read(buffer)) != -1)
			        aFileOutputStream.write(buffer, 0, length);
	
				aFileInputStream.close();
				aFileOutputStream.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			// Dernière étape : on détruit le fichier de travail :
			File aWorkingDeleteFile = new File(aWorkingFileString);
			aWorkingDeleteFile.delete();
		}
	
		return;
	}

}
