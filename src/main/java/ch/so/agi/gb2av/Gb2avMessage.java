package ch.so.agi.gb2av;

import java.util.Date;

public class Gb2avMessage {
	private String t_id;
	private String t_datasetname;
	private String nummer;
	private String nbident;
	private String status;
	private String bemerkungen;
	private Date grundbucheintrag;
	private Date tagebucheintrag;
	private String tagebuchbeleg;
	private Date importdatum;
	public String getT_id() {
		return t_id;
	}
	public void setT_id(String t_id) {
		this.t_id = t_id;
	}
	public String getT_datasetname() {
		return t_datasetname;
	}
	public void setT_datasetname(String t_datasetname) {
		this.t_datasetname = t_datasetname;
	}
	public String getNummer() {
		return nummer;
	}
	public void setNummer(String nummer) {
		this.nummer = nummer;
	}
	public String getNbident() {
		return nbident;
	}
	public void setNbident(String nbident) {
		this.nbident = nbident;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getBemerkungen() {
		return bemerkungen;
	}
	public void setBemerkungen(String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}
	public Date getGrundbucheintrag() {
		return grundbucheintrag;
	}
	public void setGrundbucheintrag(Date grundbucheintrag) {
		this.grundbucheintrag = grundbucheintrag;
	}
	public Date getTagebucheintrag() {
		return tagebucheintrag;
	}
	public void setTagebucheintrag(Date tagebucheintrag) {
		this.tagebucheintrag = tagebucheintrag;
	}
	public String getTagebuchbeleg() {
		return tagebuchbeleg;
	}
	public void setTagebuchbeleg(String tagebuchbeleg) {
		this.tagebuchbeleg = tagebuchbeleg;
	}
	public Date getImportdatum() {
		return importdatum;
	}
	public void setImportdatum(Date importdatum) {
		this.importdatum = importdatum;
	}
}
