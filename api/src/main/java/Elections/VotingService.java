package Elections;

import Elections.Exceptions.AlreadyFinishedElectionException;
import Elections.Exceptions.ElectionStateException;
import Elections.Exceptions.ElectionsAlreadyStartedException;
import Elections.Models.Vote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface VotingService extends Remote {

    int bulkSize = 1000;
    String SERVICE_NAME = "voting_service" ;

    /**
     * We suggest that no more than bulkSize votes are sent per request
     * If already open
     *
     * @param votes List with votes to be counted
     * @throws ElectionsAlreadyStartedException or
     * @throws AlreadyFinishedElectionException can be thrown
     */
    void vote(List<Vote> votes) throws RemoteException, ElectionStateException;

    /**
     * We suggest that no more than bulkSize votes are sent per request
     * If already open
     *
     * @param vote vote to be counted
     * @throws ElectionsAlreadyStartedException or
     * @throws AlreadyFinishedElectionException can be thrown
     */
    void vote(Vote vote) throws ElectionStateException, RemoteException;
}
