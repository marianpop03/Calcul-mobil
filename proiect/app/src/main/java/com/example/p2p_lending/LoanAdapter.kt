package com.example.p2p_lending

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener




class LoanAdapter(private val loans: List<Loan>, private val currentUser: String) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    private val databaseUrl = "https://p2p-lending-app-8d324-default-rtdb.europe-west1.firebasedatabase.app"

    class LoanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvBorrowerName)
        val tvPurpose: TextView = view.findViewById(R.id.tvPurpose)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvProgressLabel: TextView = view.findViewById(R.id.tvProgressLabel)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val btnInvest: Button = view.findViewById(R.id.btnInvest)
        val tvInterestRate: TextView = view.findViewById(R.id.tvInterestRate)
        val btnMenu: ImageView = view.findViewById(R.id.btnMenu)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loan, parent, false)
        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loans[position]
        if (loan.borrowerName == currentUser) {
            holder.btnMenu.visibility = View.VISIBLE

            holder.btnMenu.setOnClickListener { view ->
                showPopupMenu(view, loan, holder.itemView.context)
            }
        } else {
            holder.btnMenu.visibility = View.GONE
        }

        holder.tvName.text = loan.borrowerName
        holder.tvPurpose.text = loan.purpose
        holder.tvAmount.text = "${loan.amount} RON"
        holder.tvInterestRate.text = "Dobândă: ${loan.interestRate}%"

        holder.tvProgressLabel.text = "Strâns: ${loan.fundedAmount.toInt()} / ${loan.amount.toInt()} RON"
        val remaining = loan.amount - loan.fundedAmount


        val progressPercentage = if (loan.amount > 0) {
            ((loan.fundedAmount / loan.amount) * 100).toInt()
        } else 0

        val animation = ObjectAnimator.ofInt(holder.progressBar, "progress", holder.progressBar.progress, progressPercentage)
        animation.duration = 1000
        animation.interpolator = DecelerateInterpolator()
        animation.start()


        if (loan.fundedAmount >= loan.amount) {
            holder.btnInvest.text = "FINANȚAT COMPLET"
            holder.btnInvest.isEnabled = false
            holder.btnInvest.setBackgroundColor(Color.GRAY)
        } else {
            holder.btnInvest.text = "INVESTEȘTE"
            holder.btnInvest.isEnabled = true
            holder.btnInvest.setBackgroundColor(0xFF2196F3.toInt()) // Albastru

            holder.btnInvest.setOnClickListener {
                showInvestDialog(holder.itemView.context, loan, remaining)
            }
        }

    }

    private fun showInvestDialog(context: Context, loan: Loan, maxAmount: Double) {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        // Câmpul de introdus suma
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Suma (Maxim: $maxAmount)"
        layout.addView(input)

        // Text care arată profitul estimat
        val profitText = TextView(context)
        profitText.text = "Introdu suma pentru calcul..."
        profitText.setPadding(0, 20, 0, 0)
        layout.addView(profitText)

        // Listener care actualizează profitul în timp ce tastezi
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                    try {
                        val amount = text.toDouble()
                        val profit = amount * (loan.interestRate / 100.0)
                        profitText.text = "Dobândă pe interval: +${String.format("%.2f", profit)} RON"
                        profitText.setTextColor(0xFF2E7D32.toInt()) // Verde
                    } catch (e: Exception) {
                        profitText.text = "..."
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        AlertDialog.Builder(context)
            .setTitle("Investește în ${loan.borrowerName}")
            .setView(layout)
            .setPositiveButton("Confirmă") { _, _ ->
                val amountString = input.text.toString()
                if (amountString.isNotEmpty()) {
                    val amountToInvest = amountString.toDouble()

                    if (amountToInvest > maxAmount) {
                        Toast.makeText(context, "Suma e prea mare! Mai sunt necesari doar $maxAmount", Toast.LENGTH_SHORT).show()
                    } else if (amountToInvest <= 0) {
                        Toast.makeText(context, "Sumă invalidă", Toast.LENGTH_SHORT).show()
                    } else {
                        // Porneste procesul de investiție
                        processInvestment(context, loan, amountToInvest)
                    }
                }
            }
            .setNegativeButton("Anulează", null)
            .show()
    }
    private fun showPopupMenu(view: View, loan: Loan, context: Context) {
        val popup = androidx.appcompat.widget.PopupMenu(context, view)
        // Creeaza meniul programatic (fără XML)
        popup.menu.add("Editează")
        popup.menu.add("Șterge")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Editează" -> {
                    showEditDialog(context, loan)
                    true
                }
                "Șterge" -> {
                    deleteLoan(context, loan)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun deleteLoan(context: Context, loan: Loan) {
        // Șterge din Firebase
        val database = FirebaseDatabase.getInstance(databaseUrl)
        database.getReference("loans").child(loan.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Împrumut șters!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditDialog(context: Context, loan: Loan) {
        // 1. Refoloseste layout-ul de la adăugare (are deja toate câmpurile)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_loan, null)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val etInterest = dialogView.findViewById<EditText>(R.id.etInterest)
        val etPurpose = dialogView.findViewById<EditText>(R.id.etPurpose)

        // 2. Pre-completeaza datele existente
        etName.setText(loan.borrowerName)
        etName.isEnabled = false // Numele nu se poate schimba

        etAmount.setText(loan.amount.toString())
        etInterest.setText(loan.interestRate.toString())
        etPurpose.setText(loan.purpose)

        AlertDialog.Builder(context)
            .setTitle("Editează Împrumutul")
            .setView(dialogView)
            .setPositiveButton("Salvează Modificări") { _, _ ->
                val newAmountStr = etAmount.text.toString()
                val newInterestStr = etInterest.text.toString()
                val newPurpose = etPurpose.text.toString()

                if (newAmountStr.isNotEmpty() && newInterestStr.isNotEmpty() && newPurpose.isNotEmpty()) {
                    val newAmount = newAmountStr.toDouble()
                    val newInterest = newInterestStr.toDouble()

                    // 3. Trimite tot la Firebase
                    val database = FirebaseDatabase.getInstance(databaseUrl)
                    val loanRef = database.getReference("loans").child(loan.id)

                    val updates = mapOf<String, Any>(
                        "amount" to newAmount,
                        "interestRate" to newInterest,
                        "purpose" to newPurpose
                    )

                    loanRef.updateChildren(updates)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Împrumut actualizat complet!", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Nu lăsa câmpuri goale!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anulează", null)
            .show()
    }
    private fun processInvestment(context: Context, loan: Loan, amount: Double) {
        val database = FirebaseDatabase.getInstance(databaseUrl)

        // Referințe către tabelele din baza de date
        val userRef = database.getReference("users").child(currentUser)
        val loanRef = database.getReference("loans").child(loan.id)
        val investmentsRef = database.getReference("user_investments").child(currentUser)

        // Citeste datele utilizatorului să vedem dacă are bani
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)

                if (user != null) {
                    if (user.balance >= amount) {
                        // --- TRANZACȚIA REUȘITĂ ---

                        // 1. Scade banii din portofelul utilizatorului
                        val newBalance = user.balance - amount
                        val newTotalInvested = user.totalInvested + amount

                        userRef.child("balance").setValue(newBalance)
                        userRef.child("totalInvested").setValue(newTotalInvested)

                        // 2. Adăugă banii la împrumut (ca să crească bara de progres)
                        val newFunded = loan.fundedAmount + amount
                        loanRef.child("fundedAmount").setValue(newFunded)

                        // 3. Creează Înregistrarea Investiției
                        val timestamp = System.currentTimeMillis()
                        val newInvId = investmentsRef.push().key ?: ""


                        val investment = Investment(
                            id = newInvId,
                            amount = amount,
                            interestRate = loan.interestRate, // Salvează procentul (ex: 10.0)
                            timestamp = timestamp,
                            claimed = false
                        )

                        investmentsRef.child(newInvId).setValue(investment)

                        Toast.makeText(context, "Investiție reușită! Banii produc bani acum.", Toast.LENGTH_LONG).show()

                    } else {

                        Toast.makeText(context, "Nu ai destui bani! Ai doar ${user.balance} RON", Toast.LENGTH_LONG).show()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Eroare de conexiune!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun getItemCount() = loans.size
}