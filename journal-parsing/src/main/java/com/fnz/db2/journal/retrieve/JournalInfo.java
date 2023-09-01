package com.fnz.db2.journal.retrieve;

public record JournalInfo(String journalName, String journalLibrary) {

	public JournalInfo(String journalName, String journalLibrary) {
		if (journalName == null || journalName.trim().length() == 0 || journalName.trim().length() > 10) {
			throw new IllegalArgumentException("receiver name must not be null and length must be <= to 10.");
		}
		if (journalLibrary == null || journalLibrary.trim().length() == 0 || journalLibrary.trim().length() > 10) {
			throw new IllegalArgumentException("receiverLibrary name must not be null and length must be <= to 10.");
		}	
		this.journalName = journalName.trim();
		this.journalLibrary = journalLibrary.trim();
	}

	@Override
	public String toString() {
		return String.format("JournalInfo [journalName=%s, journalLibrary=%s]", journalName, journalLibrary);
	}
}
