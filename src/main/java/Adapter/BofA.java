package Adapter;

import java.util.HashMap;
import java.util.Map;

public class BofA {
	private static final String NAME = "Bank of America";
	
	public static class Account {
		public    final String name;
		protected final int    ssn;
		
		private Account(String name, int ssn) {
			this.name = name;
			this.ssn  = ssn;
		}
	}
	private Map<Integer, Account> accounts;
	private Map<Integer, Integer> balances;

	public String getName() {
		return NAME;
	}
	public Map<Integer, Account> getAccounts() {
		return accounts;
	}
	public Map<Integer, Integer> getBalances() {
		return balances;
	}

	public BofA() {
		accounts = new HashMap<>();
		accounts.put(  1, new Account( "Starbuck",         123456789 ));
		accounts.put(  5, new Account( "Number Six",       600600600 ));
		accounts.put(  6, new Account( "President Palmer", 897012399 ));
		accounts.put(  8, new Account( "Boomer",           777521002 ));
		accounts.put( 10, new Account( "Commander Adama",  988114363 ));
		accounts.put( 12, new Account( "President Palmer", 897012399 ));
		
		balances = new HashMap<>();
		balances.put(  1,   500 );
		balances.put(  5,    20 );
		balances.put(  6,   600 );
		balances.put(  8,   750 );
		balances.put( 10, 2_000 );
		balances.put( 12, 7_500 );
	}
}
