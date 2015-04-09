using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.WindowsAzure;
using Microsoft.WindowsAzure.Diagnostics;
using Microsoft.WindowsAzure.ServiceRuntime;
using Microsoft.WindowsAzure.Storage;
using System.IO;
using Microsoft.WindowsAzure.Storage.Table;

namespace Collector
{
    public class WorkerRole : RoleEntryPoint
    {
        private string quoteLink;

        private Timer timer;
        private WebClient wc = new WebClient();
        private CloudTable table;

        public override void Run()
        {
            Trace.TraceInformation("Collector is running");

            AutoResetEvent autoEvent = new AutoResetEvent(false);
            this.quoteLink = this.GetQuoteLink();

            CloudStorageAccount storageAccount = CloudStorageAccount.Parse(
                CloudConfigurationManager.GetSetting("StorageConnectionString"));

            // Create the table client.
            CloudTableClient tableClient = storageAccount.CreateCloudTableClient();

            // Create the table if it doesn't exist.
            this.table = tableClient.GetTableReference("sp500");
            this.table.CreateIfNotExists();

            this.timer = new Timer(CollectQuotes, autoEvent, 0, 60 * 1000);

            while (true)
            {
                autoEvent.WaitOne();
            }
        }

        public override bool OnStart()
        {
            ServicePointManager.DefaultConnectionLimit = 12;

            bool result = base.OnStart();

            Trace.TraceInformation("Collector has been started");

            return result;
        }

        public override void OnStop()
        {
            Trace.TraceInformation("Collector is stopping");

            timer.Dispose();
            base.OnStop();

            Trace.TraceInformation("Collector has stopped");
        }

        private string GetQuoteLink(){
            string sp500link = "http://cdn1.indicatorguys.com/downloads/sp500-symbol-list.txt";
            using (StreamReader sr = new StreamReader(wc.OpenRead(sp500link)))
            {
                string param = string.Join("+", sr.ReadToEnd().Split().Where(s => !string.IsNullOrWhiteSpace(s)));
                return "http://download.finance.yahoo.com/d/quotes.csv?s=" + param + "&f=sl1";
            }
        }

        private void CollectQuotes(Object stateInfo)
        {
            AutoResetEvent autoEvent = (AutoResetEvent)stateInfo;
            autoEvent.Set();

            DateTime snapshotTime = DateTime.UtcNow;
            TableBatchOperation batchOperation = new TableBatchOperation();

            foreach (var kv in getQuotes(this.quoteLink))
            {
                batchOperation.InsertOrReplace(new QuoteEntity(snapshotTime, kv.Key, kv.Value));

                if (batchOperation.Count == 100)
                {
                    this.table.ExecuteBatch(batchOperation);
                    batchOperation.Clear();
                }
            }

            if (batchOperation.Count > 0)
            {
                this.table.ExecuteBatch(batchOperation);
            }
        }

        private static Dictionary<String, double> getQuotes(String quoteLink)
        {
            WebClient wc = new WebClient();
            using (StreamReader sr = new StreamReader(wc.OpenRead(quoteLink)))
            {
                return sr.ReadToEnd()
                         .Split(new char[] { '\r', '\n' })
                         .Select(l => l.Split(','))
                         .Where(l => l.Length == 2)
                         .Where(l => l[1] != "N/A")
                         .ToDictionary(l => l[0].Trim('\"'), l => Double.Parse(l[1]));
            }
        }
    }
}
