package com.fnz.db2.journal.retrieve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fnz.db2.journal.retrieve.rnrn0200.DetailedJournalReceiver;
import com.fnz.db2.journal.retrieve.rnrn0200.JournalReceiverInfo;
import com.fnz.db2.journal.retrieve.rnrn0200.JournalStatus;
import com.ibm.as400.access.AS400;


@ExtendWith(MockitoExtension.class)
class JournalReceiversTest {
	JournalReceivers receivers;
	@Mock JournalInfoRetrieval journalInfoRetrieval;
	JournalInfo journalInfo = new JournalInfo("journal", "journallib");
	@Mock AS400 as400;
	
	DetailedJournalReceiver dr3 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j3", "jlib"), new Date(3), 
			JournalStatus.Attached, Optional.of(1)), BigInteger.ONE, BigInteger.valueOf(9), Optional.empty(), 1, 1);
	DetailedJournalReceiver dr2 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j2", "jlib"), new Date(2), 
			JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.ONE, BigInteger.valueOf(6), Optional.of(dr3.info().receiver()), 1, 1);
	DetailedJournalReceiver dr1 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j1", "jlib"), new Date(1),
			JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.ONE, BigInteger.TWO, Optional.of(dr2.info().receiver()),1, 1);
	
	@BeforeEach
	public void setUp() throws Exception {
	}
	
	@Test
	void findRangeWithinCurrentPosistion() throws Exception {
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, 100, journalInfo);
		
		List<DetailedJournalReceiver> list = Arrays.asList(dr1);

		when(journalInfoRetrieval.getReceivers(any(), any())).thenReturn(list);
		
		when(journalInfoRetrieval.getCurrentDetailedJournalReceiver(any(), any())).thenReturn(dr1);
		
		JournalProcessedPosition startPosition = new JournalProcessedPosition(BigInteger.ONE, new JournalReceiver("j1", "jlib"), Instant.ofEpochSecond(0), true);
		Optional<PositionRange> result = jreceivers.findRange(as400, startPosition);
		assertTrue(result.isPresent());
		PositionRange rangeAnswer = new PositionRange(startPosition, new JournalPosition(dr1.end(), dr1.info().receiver())); 
		assertEquals(Optional.of(rangeAnswer), result);
	}
	
	@Test
	void findRangeInList() throws Exception {
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, 100, journalInfo);
		
		List<DetailedJournalReceiver> list = Arrays.asList(
				dr1,
				dr2
				);

		when(journalInfoRetrieval.getReceivers(any(), any())).thenReturn(list);
		
		when(journalInfoRetrieval.getCurrentDetailedJournalReceiver(any(), any())).thenReturn(dr2);
		
		JournalProcessedPosition startPosition = new JournalProcessedPosition(BigInteger.ONE, new JournalReceiver("j1", "jlib"), Instant.ofEpochSecond(0), true);
		Optional<PositionRange> result = jreceivers.findRange(as400, startPosition);
		assertTrue(result.isPresent());
		PositionRange rangeAnswer = new PositionRange(startPosition, new JournalPosition(dr2.end(), dr2.info().receiver())); 
		assertEquals(Optional.of(rangeAnswer), result);
	}
	
	@Test
	void findRangeReFetchList() throws Exception {
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, 15, journalInfo);
		
		DetailedJournalReceiver detailedEnd = dr2;
		List<DetailedJournalReceiver> list = Arrays.asList(
				dr1,
				detailedEnd
				);

		DetailedJournalReceiver detailedEnd2 = dr3;
		List<DetailedJournalReceiver> list2 = Arrays.asList(
				dr1,
				dr2,
				detailedEnd2
				);
		when(journalInfoRetrieval.getReceivers(any(), any())).thenReturn(list).thenReturn(list2);
		when(journalInfoRetrieval.getCurrentDetailedJournalReceiver(any(), any())).thenReturn(detailedEnd).thenReturn(detailedEnd2);
		
		JournalProcessedPosition startPosition = new JournalProcessedPosition(BigInteger.ONE, new JournalReceiver("j1", "jlib"), Instant.ofEpochSecond(0), true);
		Optional<PositionRange> result = jreceivers.findRange(as400, startPosition);
		assertTrue(result.isPresent());
		PositionRange rangeAnswer = new PositionRange(startPosition, new JournalPosition(BigInteger.valueOf(6), detailedEnd.info().receiver())); 
		assertEquals(Optional.of(rangeAnswer), result);
		
		
		JournalProcessedPosition startPosition2 = new JournalProcessedPosition(BigInteger.valueOf(2), new JournalReceiver("j2", "jlib"), Instant.ofEpochSecond(0), true);
		Optional<PositionRange> result2 = jreceivers.findRange(as400, startPosition2);
		assertTrue(result2.isPresent());
		PositionRange rangeAnswer2 = new PositionRange(startPosition2, new JournalPosition(BigInteger.valueOf(9), detailedEnd2.info().receiver())); 
		assertEquals(Optional.of(rangeAnswer2), result2);

	}
	
	
	@Test
	void testFindRangeMidFirstEntry() {
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, 100, journalInfo);
		DetailedJournalReceiver j1 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j1", "jlib"), new Date(1), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(5), Optional.of(new JournalReceiver("j2", "jlib")), 1, 1);
		DetailedJournalReceiver j2 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j2", "jlib"), new Date(2), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(6), BigInteger.valueOf(20), Optional.of(new JournalReceiver("j3", "jlib")), 1, 1);
		DetailedJournalReceiver j3 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j3", "jlib"), new Date(3), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(21), BigInteger.valueOf(22), Optional.empty(), 1, 1);
		List<DetailedJournalReceiver> list = Arrays.asList(j1, j2, j3);
		Optional<JournalPosition> position = jreceivers.findPosition(new JournalProcessedPosition(BigInteger.ONE, j1.info().receiver(), Instant.ofEpochSecond(0), true), BigInteger.valueOf(3), list, j3);
		assertTrue(position.isPresent());
		assertEquals(BigInteger.valueOf(4), position.get().getOffset());
	}


	@Test
	void testFindRangeMidSecondEntryReset() {
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, 100, journalInfo);
		DetailedJournalReceiver j1 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j1", "jlib"), new Date(1), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(10), Optional.of(new JournalReceiver("j2", "jlib")), 1, 1);
		DetailedJournalReceiver j2 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j2", "jlib"), new Date(2), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(10), Optional.of(new JournalReceiver("j3", "jlib")), 1, 1);
		DetailedJournalReceiver j3 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j3", "jlib"), new Date(3), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(10), Optional.empty(), 1, 1);
		List<DetailedJournalReceiver> list = Arrays.asList(j1, j2, j3);
		Optional<JournalPosition> position = jreceivers.findPosition(new JournalProcessedPosition(BigInteger.ONE, j1.info().receiver(), Instant.ofEpochSecond(0), true), BigInteger.valueOf(15), list, j3);
		assertTrue(position.isPresent());
		assertEquals(BigInteger.valueOf(6), position.get().getOffset());
	}
	
	@Test
	void testFindRangeMidSecondContiguous() {
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, 100, journalInfo);
		DetailedJournalReceiver j1 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j1", "jlib"), new Date(1), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(2), Optional.of(new JournalReceiver("j2", "jlib")), 1, 1);
		DetailedJournalReceiver j2 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j2", "jlib"), new Date(2), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(3), BigInteger.valueOf(20), Optional.of(new JournalReceiver("j3", "jlib")), 1, 1);
		DetailedJournalReceiver j3 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j3", "jlib"), new Date(3), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(21), BigInteger.valueOf(22), Optional.empty(), 1, 1);
		List<DetailedJournalReceiver> list = Arrays.asList(j1, j2, j3);
		Optional<JournalPosition> position = jreceivers.findPosition(new JournalProcessedPosition(BigInteger.ONE, j1.info().receiver(), Instant.ofEpochSecond(0), true), BigInteger.valueOf(10), list, j3);
		assertTrue(position.isPresent());
		assertEquals(BigInteger.valueOf(11), position.get().getOffset());
	}

	@Test
	void testFindRangeMidEndEntry() {
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, 100, journalInfo);
		DetailedJournalReceiver j1 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j1", "jlib"), new Date(1), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(2), Optional.of(new JournalReceiver("j2", "jlib")), 1, 1);
		DetailedJournalReceiver j2 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j2", "jlib"), new Date(2), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(3), BigInteger.valueOf(20), Optional.of(new JournalReceiver("j3", "jlib")), 1, 1);
		DetailedJournalReceiver j3 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j3", "jlib"), new Date(3), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(21), BigInteger.valueOf(35), Optional.empty(), 1, 1);
		List<DetailedJournalReceiver> list = Arrays.asList(j1, j2, j3);
		Optional<JournalPosition> position = jreceivers.findPosition(new JournalProcessedPosition(BigInteger.ONE, j1.info().receiver(), Instant.ofEpochSecond(0), true), BigInteger.valueOf(30), list, j3);
		assertTrue(position.isPresent());
		assertEquals(BigInteger.valueOf(31), position.get().getOffset());
	}
	
	@Test
	void testFindRangePastEnd() {
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, 100, journalInfo);
		DetailedJournalReceiver j1 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j1", "jlib"), new Date(1), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(2), Optional.of(new JournalReceiver("j2", "jlib")), 1, 1);
		DetailedJournalReceiver j2 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j2", "jlib"), new Date(2), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(3), BigInteger.valueOf(20), Optional.of(new JournalReceiver("j3", "jlib")), 1, 1);
		DetailedJournalReceiver j3 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j3", "jlib"), new Date(3), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(21), BigInteger.valueOf(35), Optional.empty(), 1, 1);
		List<DetailedJournalReceiver> list = List.of(j1, j2, j3);
		Optional<JournalPosition> position = jreceivers.findPosition(new JournalProcessedPosition(BigInteger.ONE, j1.info().receiver(), Instant.ofEpochSecond(0), true), BigInteger.valueOf(100), list, j3);
		assertTrue(position.isPresent());
		assertEquals(BigInteger.valueOf(35), position.get().getOffset());
	}
	
	@Test
	void testFindMidStartingMid() {
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, 100, journalInfo);
		DetailedJournalReceiver j1 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j1", "jlib"), new Date(1), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(10), Optional.of(new JournalReceiver("j2", "jlib")), 1, 1);
		DetailedJournalReceiver j2 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j2", "jlib"), new Date(2), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(11), BigInteger.valueOf(20), Optional.of(new JournalReceiver("j3", "jlib")), 1, 1);
		DetailedJournalReceiver j3 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j3", "jlib"), new Date(3), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(21), BigInteger.valueOf(31), Optional.of(new JournalReceiver("j4", "jlib")), 1, 1);
		DetailedJournalReceiver j4 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j4", "jlib"), new Date(4), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(41), BigInteger.valueOf(50), Optional.empty(), 1, 1);
		List<DetailedJournalReceiver> list = List.of(j1, j2, j3, j4);
		Optional<JournalPosition> position = jreceivers.findPosition(new JournalProcessedPosition(BigInteger.valueOf(10), j3.info().receiver(), Instant.ofEpochSecond(0), true), BigInteger.valueOf(15), list, j4);
		assertTrue(position.isPresent());
		assertEquals(BigInteger.valueOf(25), position.get().getOffset());

	}
	
	@Test
	void testFindStartingPastEnd() {
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, 100, journalInfo);
		DetailedJournalReceiver j1 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j1", "jlib"), new Date(1), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(10), Optional.of(new JournalReceiver("j2", "jlib")), 1, 1);
		DetailedJournalReceiver j2 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j2", "jlib"), new Date(2), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(11), BigInteger.valueOf(20), Optional.empty(), 1, 1);
		List<DetailedJournalReceiver> list = List.of(j1, j2);
		Optional<JournalPosition> position = jreceivers.findPosition(new JournalProcessedPosition(BigInteger.valueOf(30), new JournalReceiver("j3", "jlib"), Instant.ofEpochSecond(0), true), BigInteger.valueOf(15), list, j2);
		assertTrue(position.isEmpty());
	}

	@Test
	void testMaxOffsetInSameReceiverEnd() throws Exception {
		int maxOffset=1000;
		BigInteger maxServerSideEntriesBI = BigInteger.valueOf(maxOffset);
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, maxOffset, journalInfo);
		
		JournalProcessedPosition startPosition = new JournalProcessedPosition(BigInteger.ONE, new JournalReceiver("j1", "jlib"), Instant.ofEpochSecond(0), true);
		DetailedJournalReceiver endJournalPosition = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j1", "jlib"), new Date(1), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(100), Optional.empty(), 1, 1);

		PositionRange range = jreceivers.maxOffsetInSameReceiver(startPosition, endJournalPosition, maxServerSideEntriesBI);
		assertEquals(endJournalPosition.end(), range.end().getOffset());
	}
	
	@Test
	void testMaxOffsetInSameReceiverLimited() throws Exception {
		int maxOffset=10;
		BigInteger maxServerSideEntriesBI = BigInteger.valueOf(maxOffset);
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, maxOffset, journalInfo);
		
		JournalProcessedPosition startPosition = new JournalProcessedPosition(BigInteger.ONE, new JournalReceiver("j1", "jlib"), Instant.ofEpochSecond(0), true);
		DetailedJournalReceiver endJournalPosition = new DetailedJournalReceiver(new JournalReceiverInfo(startPosition.getReciever(), new Date(1), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(100), Optional.empty(), 1, 1);

		PositionRange range = jreceivers.maxOffsetInSameReceiver(startPosition, endJournalPosition, maxServerSideEntriesBI);
		assertEquals(startPosition.getOffset().add(maxServerSideEntriesBI), range.end().getOffset());
	}
	
	@Test
	void testUpdateEndPosition() {
		DetailedJournalReceiver j1 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j1", "jlib"), new Date(1), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(10), Optional.of(new JournalReceiver("j2", "jlib")), 1, 1);
		DetailedJournalReceiver j2 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j2", "jlib"), new Date(2), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(11), BigInteger.valueOf(20), Optional.empty(), 1, 1);
		List<DetailedJournalReceiver> list = Arrays.asList(j1, j2);

		DetailedJournalReceiver endPosition = new DetailedJournalReceiver(new JournalReceiverInfo(j2.info().receiver(), new Date(2), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(11), BigInteger.valueOf(200), Optional.empty(), 1, 1);
		JournalReceivers.updateEndPosition(list, endPosition);
		
		assertEquals(endPosition, list.get(1));
	}

	
	@Test
	void testFindMissingCurrentReceiver() {
		JournalReceivers jreceivers = new JournalReceivers(journalInfoRetrieval, 100, journalInfo);
		DetailedJournalReceiver j1 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j1", "jlib"), new Date(1), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(1), BigInteger.valueOf(10), Optional.of(new JournalReceiver("j2", "jlib")), 1, 1);
		DetailedJournalReceiver j2 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j2", "jlib"), new Date(2), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(11), BigInteger.valueOf(20), Optional.empty(), 1, 1);
		DetailedJournalReceiver j3 = new DetailedJournalReceiver(new JournalReceiverInfo(new JournalReceiver("j3", "jlib"), new Date(3), JournalStatus.OnlineSavedDetached, Optional.of(1)), BigInteger.valueOf(21), BigInteger.valueOf(31), Optional.of(new JournalReceiver("j4", "jlib")), 1, 1);
		List<DetailedJournalReceiver> list = List.of(j1, j2);
		Optional<JournalPosition> position = jreceivers.findPosition(new JournalProcessedPosition(BigInteger.valueOf(30), j1.info().receiver(), Instant.ofEpochSecond(0), true), BigInteger.valueOf(15), list, j3);
		assertTrue(position.isEmpty());
	}
}
