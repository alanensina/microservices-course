package alanensina.inventoryservice;

import alanensina.inventoryservice.models.Inventory;
import alanensina.inventoryservice.repositories.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
        return args -> {
            var inventory1 = new Inventory();
            inventory1.setId(1L);
            inventory1.setSkuCode("ps5");
            inventory1.setQuantity(200);

            var inventory2 = new Inventory();
            inventory2.setId(2L);
            inventory2.setSkuCode("iphone14");
            inventory2.setQuantity(150);

            var inventory3 = new Inventory();
            inventory3.setId(3L);
            inventory3.setSkuCode("iphone6");
            inventory3.setQuantity(0);

            inventoryRepository.save(inventory1);
            inventoryRepository.save(inventory2);
            inventoryRepository.save(inventory3);
        };
    }
}
