package es.deusto.ssdd.tracker.metainfo.handler;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import es.deusto.ssdd.tracker.metainfo.InfoDictionary;
import es.deusto.ssdd.tracker.metainfo.MetainfoFile;
import es.deusto.ssdd.tracker.metainfo.bencoding.Bencoder;


public abstract class MetainfoHandler<Info extends InfoDictionary> {
	private Bencoder bencoder;
	private MetainfoFile<Info> metainfo;
	
	public MetainfoHandler() {
		this.bencoder = new Bencoder();
		this.metainfo = new MetainfoFile<Info>();
	}
	
	protected Bencoder getBencoder() {
		return this.bencoder;
	}
	
	public MetainfoFile<Info> getMetainfo() {
		return this.metainfo;
	}
	
	protected void setMetainfo(MetainfoFile<Info> metainfo) {
		this.metainfo = metainfo;
	}
	
	@SuppressWarnings("unchecked")
	public void parseTorrenFile(String filename) {
		byte[] fileBytes = MetainfoHandler.fileToByteArray(filename);
		
		HashMap<String, Object> dictionary = bencoder.unbencodeDictionary(fileBytes);
		
		if (dictionary.containsKey("announce")) {
			this.metainfo.setAnnounce((String)dictionary.get("announce"));
		}
		
		if (dictionary.containsKey("announce-list")) {
			this.metainfo.setAnnounceList(((List<List<String>>)dictionary.get("announce-list")));
		}
		
		if (dictionary.containsKey("creation date")) {
			this.metainfo.setCreationDate((Integer)dictionary.get("creation date"));
		}
		
		if (dictionary.containsKey("comment")) {
			this.metainfo.setComment((String)dictionary.get("comment"));
		}
		
		if (dictionary.containsKey("created by")) {
			this.metainfo.setComment((String)dictionary.get("created by"));
		}
		
		if (dictionary.containsKey("encoding")) {
			this.metainfo.setComment((String)dictionary.get("encoding"));
		}
		
		this.parseInfo((HashMap<String, Object>)dictionary.get("info"));
		
		byte[] infoHash = bencoder.generateHash(fileBytes, "4:info");
		
		if (infoHash != null) {
			this.metainfo.getInfo().setInfoHash(infoHash);
			this.metainfo.getInfo().setUrlInfoHash(MetainfoHandler.toURLEncodedString(infoHash));
			this.metainfo.getInfo().setHexInfoHash(MetainfoHandler.toHexString(infoHash));
		}
	}	
	
	protected abstract void parseInfo(HashMap<String, Object> info);
	
	protected void parsePieces(String piecesString) {
		if (piecesString.length() % 20 != 0) {
			System.err.println("# [MetainfoSingleFileHandler]: Length of the SHA-1 hash for the file's pieces is incorrect.");
			return;
		}

		byte[] stringBytes = piecesString.getBytes(); 
		byte[] individualHash;		
		int numPieces = piecesString.length() / 20;

		for (int i = 0; i < numPieces; i++) {
			individualHash = new byte[20];
			
			for (int j = 0; j < 20; j++) {
				individualHash[j] = stringBytes[(20 * i) + j];
			}
			
			this.metainfo.getInfo().getByteSHA1().add(individualHash);
			this.metainfo.getInfo().getHexStringSHA1().add(MetainfoHandler.toHexString(individualHash));
			this.metainfo.getInfo().getUrlEncodedSHA1().add(MetainfoHandler.toURLEncodedString(individualHash));
		}
	}
	
	private static byte[] fileToByteArray(String filename) {
		byte[] result = null;
		
		if (filename != null) {
			try (FileInputStream fis = new FileInputStream(filename);
				 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {		        
				byte[] buf = new byte[1024];
		        
				for (int readNum; (readNum = fis.read(buf)) != -1;) {
					bos.write(buf, 0, readNum);
	            }

				result = bos.toByteArray();
			} catch (IOException e) {
				System.err.println("# Error loading a metainfo file as an array of bytes: " + e.getMessage());
			}
		}
		
		return result;
	}
	
	//Hexadecimal characters
	static final String HEX_DIGITS = "0123456789ABCDEF";
	
	public static String toURLEncodedString(byte[] bytes ) {
        StringBuffer result = new StringBuffer(bytes.length*2);
        char c;
        
        for( int i = 0; i < bytes.length; i++ ) {
            c = (char) bytes[i];

            switch( c ) {
            	case '.':	
            	case '-':  
            	case '_':
            	case '~':
            		result.append(c);
            		break;
              default:
                if( (c >= 'a' && c <= 'z') || 
                	(c >= 'A' && c <= 'Z') || 
                	(c >= '0' && c <= '9') ) {
                    result.append(c);
                } else {
                    result.append('%');
                    result.append(HEX_DIGITS.charAt((c & 0xF0) >> 4 ));
                    result.append(HEX_DIGITS.charAt(c & 0x0F));
                }
            }
        }

        return result.toString();
    }
	
	public static String toHexString(byte[] bytes) {
		return DatatypeConverter.printHexBinary(bytes);
	}
}