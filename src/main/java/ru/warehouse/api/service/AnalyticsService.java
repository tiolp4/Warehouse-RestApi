package ru.warehouse.api.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.warehouse.api.dto.Dtos.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final EntityManager em;

    public AnalyticsService(EntityManager em) { this.em = em; }

    public KpiDto kpi() {
        Object[] r = (Object[]) em.createNativeQuery("""
            SELECT
                (SELECT COUNT(*) FROM transit_shipments),
                (SELECT COUNT(*) FROM transit_shipments WHERE status='IN_TRANSIT'),
                (SELECT COUNT(*) FROM transit_shipments WHERE status='DELIVERED'),
                (SELECT COUNT(*) FROM transit_shipments WHERE status='DELAYED'),
                (SELECT COUNT(*) FROM logistics_schedules),
                (SELECT COUNT(*) FROM logistics_schedules WHERE work_date=CURRENT_DATE),
                (SELECT COUNT(*) FROM logistics_schedules WHERE status='COMPLETED'),
                (SELECT COUNT(*) FROM logistics_schedules WHERE status IN ('SCHEDULED','IN_PROGRESS')),
                (SELECT COUNT(*) FROM invoices),
                (SELECT COUNT(*) FROM invoices WHERE status='CONFIRMED'),
                (SELECT COALESCE(SUM(COALESCE(ii.actual_quantity, ii.quantity)),0)
                   FROM invoice_items ii JOIN invoices i ON i.id=ii.invoice_id
                   WHERE i.type='INCOMING' AND i.status='CONFIRMED'),
                (SELECT COALESCE(SUM(COALESCE(ii.actual_quantity, ii.quantity)),0)
                   FROM invoice_items ii JOIN invoices i ON i.id=ii.invoice_id
                   WHERE i.type='OUTGOING' AND i.status='CONFIRMED')
            """).getSingleResult();
        return new KpiDto(
                num(r[0]), num(r[1]), num(r[2]), num(r[3]),
                num(r[4]), num(r[5]), num(r[6]), num(r[7]),
                num(r[8]), num(r[9]), num(r[10]), num(r[11]));
    }

    @SuppressWarnings("unchecked")
    public List<DailyFlowDto> shipmentFlow(int days) {
        Map<LocalDate, long[]> tmp = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) tmp.put(LocalDate.now().minusDays(i), new long[]{0,0});
        for (Object o : em.createNativeQuery("""
                SELECT departure_date, COUNT(*) FROM transit_shipments
                WHERE departure_date >= CURRENT_DATE - (?1 || ' days')::interval
                GROUP BY departure_date
                """).setParameter(1, days).getResultList()) {
            Object[] r = (Object[]) o;
            tmp.computeIfAbsent(((Date) r[0]).toLocalDate(), k -> new long[]{0,0})[0] = num(r[1]);
        }
        for (Object o : em.createNativeQuery("""
                SELECT actual_arrival, COUNT(*) FROM transit_shipments
                WHERE actual_arrival IS NOT NULL
                  AND actual_arrival >= CURRENT_DATE - (?1 || ' days')::interval
                GROUP BY actual_arrival
                """).setParameter(1, days).getResultList()) {
            Object[] r = (Object[]) o;
            tmp.computeIfAbsent(((Date) r[0]).toLocalDate(), k -> new long[]{0,0})[1] = num(r[1]);
        }
        List<DailyFlowDto> out = new ArrayList<>();
        tmp.forEach((d,v) -> out.add(new DailyFlowDto(d, v[0], v[1])));
        return out;
    }

    @SuppressWarnings("unchecked")
    public List<DailyFlowDto> invoiceFlow(int days) {
        var rows = em.createNativeQuery("""
            SELECT DATE(COALESCE(confirmed_at, created_at)) AS d,
                   type, COUNT(*) AS cnt
            FROM invoices
            WHERE COALESCE(confirmed_at, created_at) >= CURRENT_DATE - (?1 || ' days')::interval
            GROUP BY d, type
            ORDER BY d
            """).setParameter(1, days).getResultList();
        Map<LocalDate, long[]> tmp = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) tmp.put(LocalDate.now().minusDays(i), new long[]{0,0});
        for (Object o : rows) {
            Object[] row = (Object[]) o;
            LocalDate d = ((Date) row[0]).toLocalDate();
            long cnt = num(row[2]);
            long[] v = tmp.computeIfAbsent(d, k -> new long[]{0,0});
            if ("INCOMING".equals(row[1])) v[0] = cnt; else v[1] = cnt;
        }
        List<DailyFlowDto> out = new ArrayList<>();
        tmp.forEach((d,v) -> out.add(new DailyFlowDto(d, v[0], v[1])));
        return out;
    }

    public List<CountEntryDto> transitByStatus() {
        return group("SELECT status, COUNT(*) FROM transit_shipments GROUP BY status");
    }

    public List<CountEntryDto> scheduleByStatus() {
        return group("SELECT status, COUNT(*) FROM logistics_schedules GROUP BY status");
    }

    public List<CountEntryDto> topCarriers(int limit) {
        return groupLimited("""
            SELECT carrier, COUNT(*) FROM transit_shipments
            GROUP BY carrier ORDER BY COUNT(*) DESC LIMIT ?1
            """, limit);
    }

    public List<CountEntryDto> topDrivers(int limit) {
        return groupLimited("""
            SELECT driver_name, COUNT(*) FROM logistics_schedules
            WHERE status IN ('COMPLETED','IN_PROGRESS')
            GROUP BY driver_name ORDER BY COUNT(*) DESC LIMIT ?1
            """, limit);
    }

    @SuppressWarnings("unchecked")
    private List<CountEntryDto> group(String sql) {
        return ((List<Object[]>) em.createNativeQuery(sql).getResultList())
                .stream()
                .map(r -> new CountEntryDto((String) r[0], num(r[1])))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<CountEntryDto> groupLimited(String sql, int limit) {
        return ((List<Object[]>) em.createNativeQuery(sql).setParameter(1, limit).getResultList())
                .stream()
                .map(r -> new CountEntryDto((String) r[0], num(r[1])))
                .toList();
    }

    private static long num(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }
}
