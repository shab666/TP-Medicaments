<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Medicament extends Model
{
    protected $fillable = [
        'nom',
        'dosage',
        'forme',
        'fabricant',
        'date_expiration'
    ];
}