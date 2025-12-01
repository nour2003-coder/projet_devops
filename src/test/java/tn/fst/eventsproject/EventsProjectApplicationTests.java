package tn.fst.eventsproject.services;

import tn.fst.eventsproject.entities.Event;
import tn.fst.eventsproject.entities.Logistics;
import tn.fst.eventsproject.entities.Participant;
import tn.fst.eventsproject.entities.Tache;
import tn.fst.eventsproject.repositories.EventRepository;
import tn.fst.eventsproject.repositories.LogisticsRepository;
import tn.fst.eventsproject.repositories.ParticipantRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    @Test
    void addParticipant_shouldSaveAndReturnParticipant() {
        Participant participant = new Participant();
        participant.setIdPart(1);
        participant.setNom("Ali");
        participant.setPrenom("Ahmed");

        when(participantRepository.save(participant)).thenReturn(participant);

        Participant result = eventServices.addParticipant(participant);

        assertNotNull(result);
        assertEquals(1, result.getIdPart());
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void addAffectEvenParticipant_withIdParticipant_shouldAssociateEventAndSave() {
        // participant existant sans events
        Participant participant = new Participant();
        participant.setIdPart(1);
        participant.setEvents(null);

        Event event = new Event();
        event.setIdEvent(10);
        event.setDescription("Conférence DevOps");

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event, 1);

        assertNotNull(result);
        assertEquals("Conférence DevOps", result.getDescription());
        // le participant doit maintenant avoir un set d'événements contenant event
        assertNotNull(participant.getEvents());
        assertTrue(participant.getEvents().contains(event));
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void addAffectLog_shouldAssociateLogisticsToEventAndSave() {
        Logistics logistics = new Logistics();
        logistics.setIdLog(5);
        logistics.setDescription("Salle");
        logistics.setPrixUnit(100f);
        logistics.setQuantite(2);
        logistics.setReserve(true);

        Event event = new Event();
        event.setIdEvent(20);
        event.setDescription("Atelier Java");
        event.setLogistics(null);

        when(eventRepository.findByDescription("Atelier Java")).thenReturn(event);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        Logistics result = eventServices.addAffectLog(logistics, "Atelier Java");

        assertNotNull(result);
        // l'event doit maintenant avoir un set de logistics contenant l'objet
        assertNotNull(event.getLogistics());
        assertTrue(event.getLogistics().contains(logistics));
        verify(eventRepository, times(1)).findByDescription("Atelier Java");
        verify(logisticsRepository, times(1)).save(logistics);
    }

    @Test
    void getLogisticsDates_shouldReturnOnlyReservedLogistics() {
        LocalDate d1 = LocalDate.of(2025, 1, 1);
        LocalDate d2 = LocalDate.of(2025, 12, 31);

        Logistics l1 = new Logistics();
        l1.setIdLog(1);
        l1.setReserve(true);

        Logistics l2 = new Logistics();
        l2.setIdLog(2);
        l2.setReserve(false);

        Event event1 = new Event();
        event1.setIdEvent(1);
        event1.setLogistics(new HashSet<>(Arrays.asList(l1, l2)));

        when(eventRepository.findByDateDebutBetween(d1, d2))
                .thenReturn(Collections.singletonList(event1));

        List<Logistics> result = eventServices.getLogisticsDates(d1, d2);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(l1));
        assertFalse(result.contains(l2));
        verify(eventRepository, times(1))
                .findByDateDebutBetween(d1, d2);
    }

    @Test
    void calculCout_shouldComputeCostForEventsOfGivenOrganisateur() {
        // event avec 2 logistiques réservées
        Logistics l1 = new Logistics();
        l1.setReserve(true);
        l1.setPrixUnit(100f);
        l1.setQuantite(2); // 200

        Logistics l2 = new Logistics();
        l2.setReserve(true);
        l2.setPrixUnit(50f);
        l2.setQuantite(1); // 50

        Logistics l3 = new Logistics();
        l3.setReserve(false);
        l3.setPrixUnit(999f);
        l3.setQuantite(1); // non compté

        Event event = new Event();
        event.setIdEvent(1);
        event.setDescription("DevOps Day");
        event.setLogistics(new HashSet<>(Arrays.asList(l1, l2, l3)));

        List<Event> events = Collections.singletonList(event);

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR
        )).thenReturn(events);

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        eventServices.calculCout();

        // somme attendue = 200 + 50 = 250
        assertEquals(250f, event.getCout());
        verify(eventRepository, times(1))
                .findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                        "Tounsi", "Ahmed", Tache.ORGANISATEUR
                );
        verify(eventRepository, times(1)).save(event);
    }
}
