package Adapter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BofAAdapter implements IBankAccounts{
	private BofA bofa;
	
	public BofAAdapter(BofA bofa) {
		this.bofa = bofa;
	}
	
	@Override
	public String getName() {
		return bofa.getName();
	}

	@Override
	public Map<Integer, Integer> getDelinquent(Integer threshold) {
		Map<Integer, Integer> delinquent = new HashMap<>();
		for(Integer key: bofa.getBalances().keySet()) {
			if (bofa.getBalances().get(key) <= threshold) {
				delinquent.put(key, bofa.getBalances().get(key));
			}
		}
		return delinquent;
	}

	@Override
	public Map<Integer, String> getNames(Set<Integer> numbers) {
		Map<Integer, String> accountName = new HashMap<>();
		for(Integer key: numbers) {
			accountName.put(key, bofa.getAccounts().get(key).name);
		}
		return accountName;
	}

}
