package listeners;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.mystore.utility.OrderContext;
import com.mystore.utility.TestResultReporter;

/**
 * 📧 ReportEmailTrigger
 * Sends HTML summary email with reports + screenshots + order details
 */
public class ReportEmailTrigger implements ISuiteListener {

    // ================= SMTP CONFIG =================
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USER = "priti.kasar@magnetoitsolutions.com";
    private static final String SMTP_PASS = "jcppaxakvelzvtwi";

    // ================= RECIPIENTS =================
    private static final String TO_EMAILS = "kaspritiautomation@gmail.com,jaimin.b@magnetoitsolutions.com,gajanan@magnetoitsolutions.com,kanisha.shah@magnetoitsolutions.com,bhargav@magnetoitsolutions.com,ravi.patel@bytestechnolab.com";
  // private static final String TO_EMAILS = "kaspritiautomation@gmail.com"; 
   private static final String CC_EMAILS = "priti.kasar+1@magnetoitsolutions.com";
    private static final String BCC_EMAILS = "pritik.magneto@gmail.com";

    // ================= FILE PATHS =================
    private static final String REPORT_HTML =
            System.getProperty("user.dir") + "/test-output/ExtentReport.html";
    private static final String SCREENSHOT_DIR =
            System.getProperty("user.dir") + "/Screenshots/";

    private static final String SUBJECT =
            "🧾 Silhouette Design Store - Production Regression Test Report | Priti Kasar";

    @Override
    public void onStart(ISuite suite) {
        System.out.println("🚀 Suite Started: " + suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {

        System.out.println("📧 Sending execution report email...");

        try {
            // Generate reports
            String csvReport = TestResultReporter.generateCSVReport(suite);
            String docxReport = TestResultReporter.generateDOCXReport(suite);

            // Setup SMTP
            Session session = Session.getInstance(getSMTPProperties(), new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER, "Automation Framework"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO_EMAILS));

            if (!CC_EMAILS.isEmpty())
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAILS));

            if (!BCC_EMAILS.isEmpty())
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(BCC_EMAILS));

            message.setSubject(SUBJECT);

            Multipart multipart = new MimeMultipart();

            // HTML BODY
            MimeBodyPart htmlBody = new MimeBodyPart();
            htmlBody.setContent(buildEmailBody(suite, multipart), "text/html; charset=utf-8");

            multipart.addBodyPart(htmlBody);

            // Attach reports
            attachIfExists(multipart, REPORT_HTML);
            attachIfExists(multipart, csvReport);
            attachIfExists(multipart, docxReport);

            message.setContent(multipart);
            Transport.send(message);

            System.out.println("✅ Email sent successfully!");

        } catch (Exception e) {
            System.err.println("❌ Email sending failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

   private String buildEmailBody(ISuite suite, Multipart multipart) {

    StringBuilder body = new StringBuilder();

    int passed = 0, failed = 0, skipped = 0;
    long totalTime = 0;

    Map<String, List<ITestResult>> moduleMap = new HashMap<>();

    // ================= DATA COLLECTION =================
    for (ISuiteResult result : suite.getResults().values()) {

        ITestContext context = result.getTestContext();

        passed += context.getPassedTests().size();
        failed += context.getFailedTests().size();
        skipped += context.getSkippedTests().size();

        totalTime += (context.getEndDate().getTime() - context.getStartDate().getTime());

        collectByModule(context.getPassedTests(), moduleMap);
        collectByModule(context.getFailedTests(), moduleMap);
        collectByModule(context.getSkippedTests(), moduleMap);
    }

    int total = passed + failed + skipped;
    String status = failed > 0 ? "🔴 FAILURES DETECTED" : "🟢 ALL CLEAR";

    body.append("<html><body style='font-family:Segoe UI;background:#f4f6f9;padding:20px;'>");

    // ================= HEADER =================
    body.append("<div style='background:#1f4e79;color:white;padding:20px;border-radius:10px;'>")
            .append("<h2>🚀 Production - Silhouette Design Store Automation</h2>")
            .append("<p>Automation Execution Report</p>")
            .append("<h3>").append(status).append("</h3>")
            .append("</div><br>");

    // ================= KPI CARDS =================
    body.append("<div style='display:flex;gap:10px;'>");

    addCard(body, "Total", total, "#007bff");
    addCard(body, "Passed", passed, "#28a745");
    addCard(body, "Failed", failed, "#dc3545");
    addCard(body, "Skipped", skipped, "#ffc107");
    addCard(body, "Time (s)", totalTime / 1000, "#6c757d");

    body.append("</div><br>");

    // ================= CHART =================
    int passPercent = total == 0 ? 0 : (passed * 100 / total);
    int failPercent = total == 0 ? 0 : (failed * 100 / total);

    body.append("<h3>📈 Execution Chart</h3>");

    body.append("<div style='background:#ddd;border-radius:5px;'>")
            .append("<div style='width:").append(passPercent)
            .append("%;background:#28a745;color:white;padding:8px;'>Passed ")
            .append(passPercent).append("%</div></div><br>");

    body.append("<div style='background:#ddd;border-radius:5px;'>")
            .append("<div style='width:").append(failPercent)
            .append("%;background:#dc3545;color:white;padding:8px;'>Failed ")
            .append(failPercent).append("%</div></div>");

    // ================= MODULE-WISE =================
    body.append("<h3>🧩 Module-wise Execution</h3>");

    for (String module : moduleMap.keySet()) {

        body.append("<div style='background:#fff;padding:10px;margin-bottom:10px;border-radius:8px;border:1px solid #ddd;'>")
                .append("<h4>").append(module).append("</h4>");

        for (ITestResult result : moduleMap.get(module)) {

            long duration = (result.getEndMillis() - result.getStartMillis()) / 1000;

            String color = result.getStatus() == ITestResult.SUCCESS ? "#28a745"
                    : result.getStatus() == ITestResult.FAILURE ? "#dc3545"
                    : "#ffc107";

            body.append("<p style='margin:5px;'>")
                    .append("<span style='color:").append(color).append(";'>●</span> ")
                    .append(result.getMethod().getDescription())
                    .append(" (").append(duration).append("s)");

            if (result.getStatus() == ITestResult.FAILURE && result.getThrowable() != null) {
                body.append("<br><span style='color:red;font-size:12px;'>")
                        .append(result.getThrowable().getMessage())
                        .append("</span>");
            }

            body.append("</p>");
        }

        body.append("</div>");
    }

    // ================= SCREENSHOTS =================
    body.append("<h3>📸 Failed Screenshots</h3>")
            .append("<div>")
            .append(attachScreenshots(multipart))
            .append("</div>");

    // ================= PRODUCT LINKS =================
    body.append("<h3>🔗 New category - Product Links (fetched first 60 products link)</h3><div style='background:#fff;padding:10px;border-radius:8px;'>");

    List<String> links = com.mystore.utility.ProductContext.getLinks();

    if (links != null && !links.isEmpty()) {
        body.append("<ol>");
        for (String link : links) {
            body.append("<li><a href='").append(link).append("'>")
                    .append(link).append("</a></li>");
        }
        body.append("</ol>");
    } else {
        body.append("<p>No links captured</p>");
    }

    body.append("</div>");

    // ================= ORDERS =================
    body.append("<h3>🧾 Orders</h3><div style='background:#fff;padding:10px;border-radius:8px;'>");

    List<String> orders = OrderContext.getAllOrders();

    if (orders != null && !orders.isEmpty()) {
        body.append("<ul>");
        for (String o : orders) {
            body.append("<li>").append(o).append("</li>");
        }
        body.append("</ul>");
    } else {
        body.append("<p>No orders created</p>");
    }

    body.append("</div>");

    // ================= FOOTER =================
    body.append("<br><p style='font-size:12px;color:gray;'>")
            .append("Executed by: <b>Priti Kasar</b><br>")
            .append("Enterprise Automation Reporting System")
            .append("</p>");

    body.append("</body></html>");

    OrderContext.clearOrders();
    com.mystore.utility.ProductContext.clear();

    return body.toString();
}

    // ================= TEST SUMMARY =================
    private String buildTestSummary(ISuite suite) {

        StringBuilder summary = new StringBuilder();

        for (ISuiteResult result : suite.getResults().values()) {

            ITestContext context = result.getTestContext();

            summary.append("<h3>📋 Test Summary</h3>")
                    .append("<ul>")
                    .append("<li><b>Total:</b> ").append(context.getAllTestMethods().length).append("</li>")
                    .append("<li style='color:green;'>Passed: ").append(context.getPassedTests().size()).append("</li>")
                    .append("<li style='color:red;'>Failed: ").append(context.getFailedTests().size()).append("</li>")
                    .append("<li style='color:orange;'>Skipped: ").append(context.getSkippedTests().size()).append("</li>")
                    .append("</ul>");

            appendTestList(summary, "✅ Passed", context.getPassedTests());
            appendTestList(summary, "❌ Failed", context.getFailedTests());
            appendTestList(summary, "⚠️ Skipped", context.getSkippedTests());
        }

        return summary.toString();
    }

    private void appendTestList(StringBuilder sb, String title, IResultMap results) {

        sb.append("<h4>").append(title).append("</h4><ul>");

        for (ITestResult result : results.getAllResults()) {
            String desc = result.getMethod().getDescription();
            if (desc != null && !desc.isEmpty()) {
                sb.append("<li>").append(desc).append("</li>");
            }
        }

        sb.append("</ul>");
    }
private void addCard(StringBuilder body, String title, long value, String color) {

    body.append("<div style='flex:1;background:")
            .append(color)
            .append(";color:white;padding:15px;border-radius:8px;text-align:center;'>")
            .append("<h3>").append(value).append("</h3>")
            .append("<p>").append(title).append("</p>")
            .append("</div>");
}
private void collectByModule(IResultMap results, Map<String, List<ITestResult>> map) {

    for (ITestResult result : results.getAllResults()) {

        String className = result.getTestClass().getName();
        String module = className.substring(className.lastIndexOf(".") + 1);

        map.computeIfAbsent(module, k -> new ArrayList<>()).add(result);
    }
}
    // ================= SCREENSHOTS =================
    private String attachScreenshots(Multipart multipart) {

        StringBuilder sb = new StringBuilder();
        File folder = new File(SCREENSHOT_DIR);

        File[] screenshots = folder.listFiles((dir, name) ->
                name.contains("_FAILED_") && name.endsWith(".png"));

        if (screenshots != null && screenshots.length > 0) {

            int i = 1;

            for (File file : screenshots) {
                String cid = "img" + i;

                sb.append("<p><b>").append(file.getName()).append("</b><br>")
                        .append("<img src='cid:").append(cid)
                        .append("' width='600'/></p>");

                try {
                    MimeBodyPart img = new MimeBodyPart();
                    img.setDataHandler(new DataHandler(new FileDataSource(file)));
                    img.setHeader("Content-ID", "<" + cid + ">");
                    img.setDisposition(MimeBodyPart.INLINE);
                    multipart.addBodyPart(img);
                } catch (Exception e) {
                    System.err.println("⚠️ Screenshot attach failed: " + e.getMessage());
                }

                i++;
            }

        } else {
            sb.append("<p>No failed screenshots.</p>");
        }

        return sb.toString();
    }

    // ================= ATTACH FILE =================
    private void attachIfExists(Multipart multipart, String path) {

        if (path == null) return;

        File file = new File(path);

        if (!file.exists()) return;

        try {
            MimeBodyPart part = new MimeBodyPart();
            part.setDataHandler(new DataHandler(new FileDataSource(file)));
            part.setFileName(file.getName());
            multipart.addBodyPart(part);

        } catch (Exception e) {
            System.err.println("⚠️ Attachment failed: " + file.getName());
        }
    }

    // ================= SMTP CONFIG =================
    private Properties getSMTPProperties() {

        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return props;
    }

    // ================= NULL SAFETY =================
    private String getSafe(String value) {
        return (value == null || value.isEmpty()) ? "N/A" : value;
    }
}
