// app.js
console.log("APP.JS LOADED ✓");

/* =========================================================
 *  DASHBOARD CHARTS
 * =======================================================*/
let mdMonthlyChart = null;
let mdQuarterlyChart = null;

function destroyChartInstance(chart) {
    if (chart && typeof chart.destroy === "function") {
        chart.destroy();
    }
}

function initDashboardCharts() {
    if (!window.MD_DASHBOARD || typeof Chart === "undefined") return;

    const { monthlyLabels, monthlyValues, quarterlyLabels, quarterlyValues } = window.MD_DASHBOARD;

    const monthlyCanvas = document.getElementById("monthlyRevenueChart");
    const quarterlyCanvas = document.getElementById("quarterlyRevenueChart");

    destroyChartInstance(mdMonthlyChart);
    destroyChartInstance(mdQuarterlyChart);

    if (monthlyCanvas && monthlyLabels && monthlyLabels.length) {
        mdMonthlyChart = new Chart(monthlyCanvas.getContext("2d"), {
            type: "line",
            data: {
                labels: monthlyLabels,
                datasets: [{
                    label: "Monthly Revenue (INR)",
                    data: monthlyValues,
                    borderWidth: 2
                }]
            }
        });
    }

    if (quarterlyCanvas && quarterlyLabels && quarterlyLabels.length) {
        mdQuarterlyChart = new Chart(quarterlyCanvas.getContext("2d"), {
            type: "bar",
            data: {
                labels: quarterlyLabels,
                datasets: [{
                    label: "Quarterly Revenue (INR)",
                    data: quarterlyValues
                }]
            }
        });
    }
}

/* =========================================================
 *  ANALYTICS CHARTS + DRILLDOWN
 * =======================================================*/
let anMonthlyChart = null;
let anQuarterlyChart = null;
let anYearlyChart = null;
let anTopChannelsChart = null;

function initAnalyticsCharts() {
    if (!window.MD_ANALYTICS || typeof Chart === "undefined") return;

    const data = window.MD_ANALYTICS;

    const mCanvas = document.getElementById("analyticsMonthlyChart");
    const qCanvas = document.getElementById("analyticsQuarterlyChart");
    const yCanvas = document.getElementById("analyticsYearlyChart");
    const tCanvas = document.getElementById("topChannelsChart");

    // MONTHLY
    if (mCanvas && data.monthlyLabels && data.monthlyLabels.length) {
        destroyChartInstance(anMonthlyChart);
        anMonthlyChart = new Chart(mCanvas.getContext("2d"), {
            type: "line",
            data: {
                labels: data.monthlyLabels,
                datasets: [{
                    label: "Monthly Client Share (INR)",
                    data: data.monthlyValues,
                    borderWidth: 2,
                    fill: false
                }]
            },
            options: { responsive: true, scales: { y: { beginAtZero: true } } }
        });
    }

    // QUARTERLY
    if (qCanvas && data.quarterlyLabels && data.quarterlyLabels.length) {
        destroyChartInstance(anQuarterlyChart);
        anQuarterlyChart = new Chart(qCanvas.getContext("2d"), {
            type: "bar",
            data: {
                labels: data.quarterlyLabels,
                datasets: [{
                    label: "Quarterly Client Share (INR)",
                    data: data.quarterlyValues
                }]
            },
            options: {
                responsive: true,
                scales: { y: { beginAtZero: true } }
            }
        });
    }

    // YEARLY
    if (yCanvas && data.yearlyLabels && data.yearlyLabels.length) {
        destroyChartInstance(anYearlyChart);
        anYearlyChart = new Chart(yCanvas.getContext("2d"), {
            type: "line",
            data: {
                labels: data.yearlyLabels,
                datasets: [{
                    label: "Yearly Client Share (INR)",
                    data: data.yearlyValues,
                    borderWidth: 2,
                    fill: false
                }]
            },
            options: { responsive: true, scales: { y: { beginAtZero: true } } }
        });
    }

    // TOP CHANNELS
    if (tCanvas && data.topChannelLabels && data.topChannelLabels.length) {
        destroyChartInstance(anTopChannelsChart);
        anTopChannelsChart = new Chart(tCanvas.getContext("2d"), {
            type: "doughnut",
            data: {
                labels: data.topChannelLabels,
                datasets: [{
                    label: "Top Channels",
                    data: data.topChannelValues
                }]
            },
            options: { responsive: true }
        });
    }
}

function initAnalyticsDrilldown() {
    const qCanvas = document.getElementById("analyticsQuarterlyChart");
    if (!qCanvas || !anQuarterlyChart) return;

    qCanvas.onclick = function (evt) {
        const points = anQuarterlyChart.getElementsAtEventForMode(
            evt,
            "nearest",
            { intersect: true },
            true
        );
        if (!points.length) return;

        const idx = points[0].index;
        const label = anQuarterlyChart.data.labels[idx]; // "2024-Q3"
        const [yearStr, quarter] = label.split("-");
        const year = parseInt(yearStr, 10);
        const channel = window.MD_ANALYTICS.selectedChannel || "";

        fetch(`/admin/analytics/quarter-details?year=${year}&quarter=${quarter}&channel=${encodeURIComponent(channel)}`)
            .then(r => r.json())
            .then(rows => {
                const tbody = document.querySelector("#quarterDetailsTable tbody");
                if (!tbody) return;
                tbody.innerHTML = "";
                if (!rows.length) {
                    tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted p-3">
                        No data for ${quarter} ${year}</td></tr>`;
                    return;
                }
                rows.forEach(r => {
                    const tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td>${r.monthName}</td>
                        <td>${r.channelName}</td>
                        <td>${r.netRevenue}</td>
                        <td>${r.inrValue}</td>
                        <td>${r.clientShare}</td>
                        <td>${r.companyShare}</td>
                        <td>${r.payableAmount}</td>
                    `;
                    tbody.appendChild(tr);
                });
            });
    };
}

/* =========================================================
 *  ADMIN REVENUE PAYMENT HELPERS (unchanged)
 * =======================================================*/
// (Your existing openPaymentDetails / openPaymentEdit / deletePayment / mark-paid
// helpers remain here – omitted for brevity if you already have them working.)


/* =========================================================
 *  REPORTS – BULK SELECTION, DOWNLOAD, DELETE
 *  (used by upload page and history page)
 * =======================================================*/
function getSelectedIds() {
    return Array.from(document.querySelectorAll("input[name='reportIds']:checked"))
        .map(cb => cb.value);
}

function toggleAll(master) {
    document.querySelectorAll("input[name='reportIds']")
        .forEach(cb => cb.checked = master.checked);
}

function downloadSelected() {
    const ids = getSelectedIds();
    if (!ids.length) return alert("Select reports to download");
    window.location = "/admin/reports/download?ids=" + ids.join(",");
}

function deleteSelected() {
    const ids = getSelectedIds();
    if (!ids.length) return alert("Select reports to delete");
    if (!confirm("Delete selected reports?")) return;

    fetch("/admin/reports/delete?ids=" + ids.join(","), { method: "DELETE" })
        .then(() => location.reload())
        .catch(() => alert("Delete failed"));
}

/* =========================================================
 *  REPORT HISTORY – LABEL-BASED PUBLISH FLOW
 * =======================================================*/
let currentPublishUploadId = null;

function loadLabelsForUpload(uploadId) {
    const container = document.getElementById("publishChannelContainer");
    const selectAll = document.getElementById("publishSelectAll");

    if (!container) return;
    container.innerHTML = `<div class="text-muted small">Loading labels…</div>`;
    if (selectAll) selectAll.checked = false;

    fetch(`/admin/reports/${uploadId}/labels`)
        .then(r => r.json())
        .then(labels => {
            if (!Array.isArray(labels) || !labels.length) {
                container.innerHTML = `<div class="text-muted small">
                    No labels found in this report.</div>`;
                return;
            }

            container.innerHTML = "";
            labels.forEach((label, idx) => {
                const safeId = `publishLabel_${idx}`;
                const div = document.createElement("div");
                div.className = "form-check";
                div.innerHTML = `
                    <input class="form-check-input publish-label"
                           type="checkbox"
                           value="${label}"
                           id="${safeId}">
                    <label class="form-check-label" for="${safeId}">
                        ${label}
                    </label>
                `;
                container.appendChild(div);
            });
        })
        .catch(err => {
            console.error(err);
            container.innerHTML = `<div class="text-danger small">
                Failed to load labels.</div>`;
        });
}

function initPublishModalBehaviour() {
    const modalElement = document.getElementById("publishModal");
    if (!modalElement) return;

    const selectAll = document.getElementById("publishSelectAll");

    if (selectAll) {
        selectAll.addEventListener("change", () => {
            document
                .querySelectorAll("#publishChannelContainer .publish-label")
                .forEach(cb => cb.checked = selectAll.checked);
        });
    }

    // Submit publish
    const publishForm = document.getElementById("publishForm");
    if (publishForm) {
        publishForm.addEventListener("submit", function (e) {
            e.preventDefault();

            const uploadId = currentPublishUploadId ||
                document.getElementById("publishUploadId").value;

            const selectedLabels = Array.from(
                document.querySelectorAll("#publishChannelContainer .publish-label:checked")
            ).map(cb => cb.value);

            if (!selectedLabels.length) {
                alert("Select at least one label (channel) to publish.");
                return;
            }

            fetch(`/admin/reports/${uploadId}/publish-channels`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(selectedLabels)
            })
                .then(r => r.ok ? r.json().catch(() => ({})) : Promise.reject())
                .then(data => {
                    // data.fullyPublished == true => all labels done, flip button
                    if (data && data.fullyPublished) {
                        const btn = document.querySelector(
                            `.publish-btn[data-id="${uploadId}"]`
                        );
                        if (btn) {
                            btn.classList.remove("btn-success");
                            btn.classList.add("btn-secondary");
                            btn.disabled = true;
                            btn.textContent = "Published";
                        }
                    }
                    $("#publishModal").modal("hide");
                })
                .catch(err => {
                    console.error(err);
                    alert("Failed to publish report.");
                });
        });
    }

    // Delegate click from table -> open modal
    document.addEventListener("click", function (e) {
        const btn = e.target.closest(".publish-btn");
        if (!btn) return;

        const uploadId = btn.dataset.id;
        currentPublishUploadId = uploadId;

        document.getElementById("publishUploadId").value = uploadId;
        document.getElementById("publishFile").value = btn.dataset.file || "";
        document.getElementById("publishType").value = btn.dataset.type || "";
        document.getElementById("publishYear").value = btn.dataset.year || "";
        document.getElementById("publishQuarter").value = btn.dataset.quarter || "";

        loadLabelsForUpload(uploadId);
        $("#publishModal").modal("show");
    });
}

/* =========================================================
 *  GLOBAL INIT
 * =======================================================*/
document.addEventListener("DOMContentLoaded", function () {

    // Initialize charts only on pages that expose the data
    if (window.MD_DASHBOARD) {
        initDashboardCharts();
    }
    if (window.MD_ANALYTICS) {
        initAnalyticsCharts();
        initAnalyticsDrilldown();
    }

    // Initialize report-history publish modal (present only on that page)
    initPublishModalBehaviour();
});
