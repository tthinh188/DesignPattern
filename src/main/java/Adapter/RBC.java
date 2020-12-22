package Adapter;



import java.util.List;

public class RBC {
	public static class Information {
		public final String name;
		public final String address;
		public final String phone;

		public Information(String name, String address, String phone) {
			this.name    = name;
			this.address = address;
			this.phone   = phone;
		}
	}
	public static class Account {
		public final int number;
		public final int balance;
		
		private Account(int number, int balance) {
			this.number = number;
			this.balance = balance;
		}
	}
	public static class Customer {
		protected String name;
		protected String sin;
		protected List<Account> accounts;

		private Customer(String name, String sin, List<Account> accounts) {
			this.name = name;
			this.sin = sin;
			this.accounts = accounts;
		}
	}
	private List<Customer> customers;
	private Information    info = new Information(
			"Royal Bank Of Canada",
			"88 Queens Quay West, Toronto, Canada",
			"1-800-769-2555" );
	
	public Information getInformation() {
		return info;
	}
	public List<Customer> getCustomers() {
		return customers;
	}
	
	public RBC() {
		customers = List.of(
		    new Customer(
				"Peter Pan",
				"680124344",
				List.of(
					new Account(  7, 2_500 ),
					new Account(  6,   350 ),
					new Account(  3, 1_900 )
		    )),
		    new Customer(
				"Captain Hook",
				"543123654",
				List.of(
					new Account(  9,     0 ),
					new Account( 11,    42 )
		    )),
		    new Customer(
		    	"Tinkerbell",
				"780321546",
				List.of(
					new Account( 2,   501 )
			))
		);
	}
}
