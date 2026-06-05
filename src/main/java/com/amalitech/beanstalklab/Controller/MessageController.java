package com.amalitech.beanstalklab.Controller;


import com.amalitech.beanstalklab.model.Message;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final DynamoDbEnhancedClient enhancedClient;
    private DynamoDbTable<Message> messageTable;

    // Inject the table name from the Beanstalk environment variable
    @Value("${DYNAMODB_TABLE_NAME:beanstalk-lab-messages}")
    private String tableName;

    public MessageController(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
    }

    /**
     * Wire up the DynamoDB table reference once the bean is initialized.
     *
     * @PostConstruct runs AFTER Spring has injected all dependencies,
     * so @Value-injected fields are available here (but not in the constructor).
     */
    @PostConstruct
    public void init() {
        this.messageTable = enhancedClient.table(tableName, TableSchema.fromBean(Message.class));
    }

    /**
     * POST /messages
     * Create a new message.
     * Body: { "text": "your message here" }
     */
    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Message message = new Message(UUID.randomUUID().toString(),       // unique ID
                text, Instant.now().toString()             // ISO timestamp
        );

        messageTable.putItem(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
    @GetMapping
    public List<Message> getMessages() {
        return messageTable.scan().items().stream().collect(Collectors.toList());
    }
    /**
     * GET /messages/{id}
     * Fetch a single message by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessage(@PathVariable String id) {
        Message message = messageTable.getItem(Key.builder().partitionValue(id).build());

        if (message == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(message);
    }
}
