package Elections.client;

import Elections.Exceptions.ElectionStateException;
import Elections.FiscalCallBack;
import Elections.FiscalService;
import Elections.Models.ElectionState;
import Elections.Models.PoliticalParty;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static java.lang.System.exit;

public class FiscalClient implements FiscalCallBack {

    @Option(name = "-DserverAddress", aliases = "--server", usage = "Fully qualified ip and port where the fiscal service is located.", required = true)
    private String ip;

    @Option(name = "-Dparty", aliases = "--partyName", usage = "Name of political party to inspect", required = true)
    private PoliticalParty party;

    @Option(name = "-Did", aliases = "--pollingPlaceNumber", usage = "Desk number to inspect", required = true)
    private Integer desk;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public PoliticalParty getParty() {
        return party;
    }

    public void setParty(PoliticalParty party) {
        this.party = party;
    }

    public Integer getDesk() {
        return desk;
    }

    public void setDesk(Integer desk) {
        this.desk = desk;
    }

    public static void main(String[] args) {
        FiscalClient client = new FiscalClient();
        try {
            CmdParserUtils.init(args, client);
        } catch (IOException e) {
            System.out.println("There was a problem reading the arguments");
            exit(1);
        }

        // Start the connection with the server
        String[] serverAddr = client.getIp().split(":", -1);
        final FiscalService is;
        try {
            final Registry registry = LocateRegistry.getRegistry(serverAddr[0], Integer.parseInt(serverAddr[1]));
            is = (FiscalService) registry.lookup(FiscalService.SERVICE_NAME);
        } catch (NotBoundException e) {
            System.out.println("There where problems finding the service needed service");
            return;
        } catch (RemoteException e) {
            System.out.println("There where problems finding the registry at ip: " + client.getIp());
            return;
        }

        // create remote object
        try {
            UnicastRemoteObject.exportObject(client, 0);
        } catch (RemoteException e) {
            System.out.println("There was a problem.");
            return;
        }

        // register client callback function
        try {
            is.addInspector(client, client.getParty(), client.getDesk());
        } catch (RemoteException e) {
            System.out.println("Could not reach service");
            return;
        } catch (ElectionStateException e) {
            System.out.println("Elections are closed or already started");
            return;
        }

        // Correctly registered
        System.out.println("Fiscal of " + client.getParty().name() + " registered on polling place " + client.getDesk());
    }

    @Override
    public void notifyVote() throws RemoteException {
        System.out.println("New vote for " + party.name() + " on pooling place " + desk.toString());
    }

    @Override
    public void endClient() throws RemoteException {
        System.out.println("Elections finished");
        exit(0);
    }

    @Override
    public void submitError(ElectionState electionState) throws RemoteException {
        if(electionState.equals(ElectionState.RUNNING)) {
            System.out.println("Elections already started");
        }
        else if(electionState.equals(ElectionState.FINISHED)) {
            System.out.println("Elections already finished");
        }
        exit(1);
    }
}
