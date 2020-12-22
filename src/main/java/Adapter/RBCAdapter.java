package Adapter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RBCAdapter implements IBankAccounts{
	private RBC rbc;
	
	public RBCAdapter(RBC rbc) {
		this.rbc = rbc;
	}
	
	@Override
	public String getName() {
		return rbc.getInformation().name;
	}

	@Override
	public Map<Integer, Integer> getDelinquent(Integer threshold) {
		Map<Integer, Integer> delinquent = new HashMap<>();
		for (RBC.Customer c: rbc.getCustomers()) {
			for(RBC.Account acc: c.accounts) {
				if(acc.balance <= threshold) {
					delinquent.put(acc.number, acc.balance);
				}
			}
		}
		return delinquent;
	}

	@Override
	public Map<Integer, String> getNames(Set<Integer> numbers) {
		Map<Integer, String> accountName = new HashMap<>();
		for(Integer key: numbers) {
			for (RBC.Customer c: rbc.getCustomers()) {
				for(RBC.Account acc: c.accounts) {
					if(acc.number == key) {
						accountName.put(key, c.name);
					}
				}
			}
		}
		return accountName;
	}

}
