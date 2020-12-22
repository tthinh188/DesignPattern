package Adapter;



import java.util.Map;
import java.util.Set;

public interface IBankAccounts {
	/**
	 * Returns the name of the bank as defined in the bank's class
	 * @return String Name of the bank.
	 */
	String                getName();
	/**
	 * Returns a map with account numbers and balances of those accounts with balances 
	 * less or equal to the given delinquency threshold.
	 * @param threshold Higher bound for a delinquent account balance.
	 * @return Map with account &lt;number,balance&gt; pairs of delinquent accounts.
	 */
	Map<Integer, Integer> getDelinquent(Integer threshold);
	/**
	 * Given a set of account numbers it returns account holder names for each of them. 
	 * @param numbers Set of account numbers.
	 * @return Map with account &lt;number,account holder's name&gt; pairs one entry per requested account number.
	 */
	Map<Integer, String>  getNames(Set<Integer> numbers);
}
