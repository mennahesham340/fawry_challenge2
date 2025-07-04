//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {

    // ---------------------- Product Classes ----------------------

    public static abstract class Product {
        protected String name;
        protected double price;
        protected int quantity;

        public Product(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }

        public void decreaseQuantity(int amount) { quantity -= amount; }
        public boolean isExpired() { return false; }
        public boolean isShippable() { return false; }
        public double getWeight() { return 0.0; }
    }

    public static class ExpirableProduct extends Product {
        private LocalDate expiryDate;
        private double weight;

        public ExpirableProduct(String name, double price, int quantity, LocalDate expiryDate, double weight) {
            super(name, price, quantity);
            this.expiryDate = expiryDate;
            this.weight = weight;
        }

        @Override
        public boolean isExpired() { return expiryDate.isBefore(LocalDate.now()); }
        @Override
        public boolean isShippable() { return true; }
        @Override
        public double getWeight() { return weight; }
    }

    public static class ShippableProduct extends Product {
        private double weight;

        public ShippableProduct(String name, double price, int quantity, double weight) {
            super(name, price, quantity);
            this.weight = weight;
        }

        @Override
        public boolean isShippable() { return true; }
        @Override
        public double getWeight() { return weight; }
    }

    public static class SimpleProduct extends Product {
        public SimpleProduct(String name, double price, int quantity) {
            super(name, price, quantity);
        }
    }

    // ---------------------- Cart and Customer ----------------------

    public static class CartItem {
        public Product product;
        public int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }

    public static class Cart {
        private List<CartItem> items = new ArrayList<>();
        public void add(Product product, int quantity) {
            if (quantity > product.getQuantity()) throw new IllegalArgumentException("Not enough stock");
            items.add(new CartItem(product, quantity));
        }
        public List<CartItem> getItems() { return items; }
        public boolean isEmpty() { return items.isEmpty(); }
    }

    public static class Customer {
        private String name;
        private double balance;
        public Customer(String name, double balance) {
            this.name = name;
            this.balance = balance;
        }
        public double getBalance() { return balance; }
        public void deductBalance(double amount) {
            if (balance < amount) throw new IllegalArgumentException("Insufficient balance");
            balance -= amount;
        }
    }

    // ---------------------- Shipping Service ----------------------

    public static class ShippingService {
        public static void shipItems(List<Product> products) {
            double totalWeight = 0;
            System.out.println(" Shipment notice ");
            for (Product p : products) {
                System.out.println(p.getName() + " " + (int) (p.getWeight() * 1000) + "g");
                totalWeight += p.getWeight();
            }
            System.out.printf("Total package weight %.1fkg\n", totalWeight);
        }
    }

    // ---------------------- Checkout ----------------------
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) throw new IllegalArgumentException("Cart is empty");

        double subtotal = 0;
        List<Product> toShip = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product p = item.product;
            if (p.isExpired()) throw new IllegalArgumentException(p.getName() + " is expired");
            if (item.quantity > p.getQuantity()) throw new IllegalArgumentException(p.getName() + " out of stock");
            subtotal += p.getPrice() * item.quantity;
            p.decreaseQuantity(item.quantity);
            if (p.isShippable()) toShip.add(p);
        }

        double shippingFee = toShip.isEmpty() ? 0 : 30;
        double total = subtotal + shippingFee;

        if (customer.getBalance() < total)
            throw new IllegalArgumentException("Insufficient balance");

        if (!toShip.isEmpty()) ShippingService.shipItems(toShip);
        customer.deductBalance(total);

        System.out.println(" Checkout receipt ");
        for (CartItem item : cart.getItems()) {
            System.out.printf("%dx %s %.0f\n", item.quantity, item.product.getName(),
                    item.product.getPrice() * item.quantity);
        }
        System.out.println("----------------------");
        System.out.printf("Subtotal %.0f\nShipping %.0f\nAmount %.0f\nEND.\n",
                subtotal, shippingFee, total);
    }

    // ---------------------- Main Test ----------------------

    public static void main(String[] args) {
        Product cheese = new ExpirableProduct("Cheese", 100, 5, LocalDate.of(2026, 8, 1), 0.4);
        Product biscuits = new ExpirableProduct("Biscuits", 150, 3, LocalDate.of(2026, 1, 1), 0.7);
        Product tv = new ShippableProduct("TV", 3000, 2, 10);
        Product scratchCard = new SimpleProduct("Scratch Card", 50, 10);

        Customer customer = new Customer("Ahmed", 5000);

        Cart cart = new Cart();
        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(scratchCard, 1);

        checkout(customer, cart);
    }
}